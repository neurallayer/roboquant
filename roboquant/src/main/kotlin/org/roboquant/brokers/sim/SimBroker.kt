/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers.sim

import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.Position
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Order

/**
 * Simulated Broker that is used as the broker during back testing and live testing. It simulates both broker and
 * exchange behavior. It can be configured with various plug-ins during initiation time that determine its behavior.
 *
 * @property initialDeposit initial deposit, default is 1 million USD
 * @param baseCurrency the base currency to use for reporting amounts, default is the (first) currency found in the
 * initial deposit
 * @property accountModel the account model (like cash or margin) to use, default is [CashAccount]
 * @constructor Create a new instance of SimBroker
 */
open class SimBroker(
    val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val accountModel: AccountModel = CashAccount()
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] denoted in a single [currencyCode].
     */
    constructor(deposit: Number, currencyCode: String = "USD") : this(
        Amount(Currency.getInstance(currencyCode), deposit).toWallet()
    )

    val closedOrders = mutableListOf<Order>()

    // Internally used account to store the state
    private val account = InternalAccount(baseCurrency)

    // Logger to use
    private val logger = Logging.getLogger(SimBroker::class)

    private var nextOrderId = 0


    init {
        this.reset()
    }

    private fun deleteOrder(order: Order) {
        account.deleteOrder(order)
        closedOrders.add(order)
    }

    fun updatePosition(asset: Asset, size: Size, price: Double) {
        val position = account.positions[asset]
        if (position == null) {
            account.positions[asset] = Position(size, price, price)
        } else {

            val newSize = position.size + size

            val avgPrice = when {
                size.sign != newSize.sign -> price

                newSize.absoluteValue > size.absoluteValue ->
                    (size.toDouble() * position.avgPrice + size.toDouble() * price) / newSize.toDouble()

                else -> position.avgPrice
            }

            account.positions[asset] = Position(newSize, avgPrice, price)
        }
    }

    private fun simulateMarket(event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        val time = event.time
        for (order in account.orders.toList()) {
            if (! order.isValid(time)) {
                deleteOrder(order)
                continue
            }
            val price = event.getPrice(order.asset)
            if (price != null) {
                if (order.isExecutable(price)) {
                    updatePosition(order.asset, order.size, price)
                    val value = order.asset.value(order.size, price)
                    account.cash.withdraw(value)
                    deleteOrder(order)
                }
            }
        }
    }

    /**
     * Run the simulation given the provided [event].
     */
    @Synchronized
    override fun sync(event: Event?): Account {
        if (event != null) {
            simulateMarket(event)
            account.updateMarketPrices(event)
            account.lastUpdate = event.time
            accountModel.updateAccount(account)
        }
        // account.orders.removeAll(newlyClosed)
        return account.toAccount()
    }

    /**
     * Place the [orders] at the broker.
     */
    @Synchronized
    override fun placeOrders(orders: List<Order>) {
        logger.trace { "Received instructions=${orders.size}" }
        for (order in orders) {
            when {
                order.isCancellation() -> {
                   val removed = account.orders.removeAll { it.id == order.id }
                    if (removed) closedOrders.add(order)
                }
                order.isModify() -> {
                    val removed  = account.orders.removeIf { it.id == order.id }
                    if (removed) account.orders.add(order)
                }
                else -> {
                    order.id = nextOrderId++.toString()
                    account.orders.add(order)
                }
            }
        }

    }


    /**
     * Reset all the state and set the cash balance back to the [initialDeposit].
     */
    fun reset() {
        account.clear()
        account.cash.deposit(initialDeposit)
        accountModel.updateAccount(account)
        closedOrders.clear()
    }

}

