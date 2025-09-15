/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.brokers

import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.common.Event
import org.roboquant.common.TIF
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Simulated Broker that is used as the broker during back testing and live testing. It simulates both broker and
 * exchange behavior. It can be configured with various plug-ins during initiation time that determine its behavior.
 *
 * @property initialDeposit initial deposit, default is 1 million USD
 * @param baseCurrency the base currency to use for reporting amounts, default is the (first) currency found in the
 * initial deposit
 * @property accountModel the account model (like cash or margin) to use, default is [CashAccountModel]
 * @property exchangeZoneId the tiemzone of the exchange, used for GTD time in force calculations
 * @constructor Create a new instance of SimBroker
 */
open class SimBroker(
    val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val accountModel: AccountModel = CashAccountModel(),
    private val exchangeZoneId: ZoneId = ZoneId.of("UTC")
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] denoted in a single [currencyCode].
     */
    constructor(deposit: Number, currencyCode: String = "USD") : this(
        Amount(Currency.getInstance(currencyCode), deposit).toWallet()
    )

    private val orderEntry: MutableMap<String, LocalDate> = mutableMapOf()

    private val pendingOrders = mutableListOf<Order>()

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
    }

    /**
     * Update the position and return the realized PNL
     */
    fun updatePosition(asset: Asset, size: Size, price: Double): Double {
        val position = account.positions[asset]

        // Open Position
        if (position == null) {
            account.positions[asset] = Position(size, price, price)
            return 0.0
        }

        val newSize = position.size + size

        // Close position
        if (newSize.iszero) {
            account.positions.remove(asset)
            return asset.value(size, position.avgPrice - price).value
        }

        // Increase position
        if (position.size.sign == size.sign) {
            val avgPrice = (size.toDouble() * position.avgPrice + size.toDouble() * price) / newSize.toDouble()
            account.positions[asset] = Position(newSize, avgPrice = avgPrice, mktPrice = price)
            return 0.0
        }

        // Decrease position
        if (size.absoluteValue <= position.size.absoluteValue) {
            account.positions[asset] = Position(newSize, position.avgPrice, price)
            return asset.value(size, position.avgPrice - price).value
        }

        // Switch position side
        account.positions[asset] = Position(newSize, price, price)
        return asset.value(position.size, price - position.avgPrice).value

    }

    private fun isExpired(order: Order, time: Instant): Boolean {
        if (order.tif == TIF.GTC) return false
        val orderDate = orderEntry[order.id]
        if (orderDate != null) {
            val currentDate = LocalDate.ofInstant(time, exchangeZoneId)
            return currentDate > orderDate
        }
        orderEntry[order.id] = LocalDate.ofInstant(time, exchangeZoneId)
        return false
    }


    private fun simulateMarket(event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        val time = event.time
        for (order in account.orders.toList()) {
            if (isExpired(order, time)) {
                deleteOrder(order)
                continue
            }
            val price = event.getPrice(order.asset)
            if (price != null) {
                if (order.isExecutable(price)) {
                    val pnl = updatePosition(order.asset, order.size, price)
                    val value = order.asset.value(order.size, price)
                    account.cash.withdraw(value)
                    val trade = Trade(order.asset, event.time, order.size, price, pnl)
                    account.trades.add(trade)
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

        for (order in pendingOrders) {
            when {
                order.isCancellation() -> {
                    val removed = account.orders.removeAll { it.id == order.id }
                    if (! removed) logger.warn("Skipping cancellation $order")
                }

                order.isModify() -> {
                    val removed = account.orders.removeIf { it.id == order.id }
                    if (removed)
                        account.orders.add(order)
                    else
                        logger.warn("Skipping modify $order")
                }

                else -> {
                    assert(order.id.isBlank())
                    order.id = nextOrderId++.toString()
                    account.orders.add(order)
                }
            }
        }
        pendingOrders.clear()
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
        logger.trace { "Received orders=${orders.size}" }
        pendingOrders.addAll(orders)
    }


    /**
     * Reset all the state and set the cash balance back to the [initialDeposit].
     */
    fun reset() {
        account.clear()
        account.cash.deposit(initialDeposit)
        accountModel.updateAccount(account)
    }

}

