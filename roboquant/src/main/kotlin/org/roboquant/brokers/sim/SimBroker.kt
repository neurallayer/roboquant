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

import org.roboquant.brokers.*
import org.roboquant.brokers.sim.execution.Execution
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import java.time.Instant

/**
 * Simulated Broker that is used as the broker during back testing and live testing. It simulates both broker and
 * exchange behavior. It can be configured with various plug-ins during initiation time that determine its behavior.
 *
 * @property initialDeposit initial deposit, default is 1 million USD
 * @param baseCurrency the base currency to use for reporting amounts, default is the (first) currency found in the
 * initial deposit
 * @property feeModel the fee/commission model to use, default is [NoFeeModel]
 * @property accountModel the account model (like cash or margin) to use, default is [CashAccount]
 * @constructor Create a new instance of SimBroker
 */
open class SimBroker(
    val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val feeModel: FeeModel = NoFeeModel(),
    private val accountModel: AccountModel = CashAccount()
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] denoted in a single [currencyCode].
     */
    constructor(deposit: Number, currencyCode: String = "USD") : this(
        Amount(Currency.getInstance(currencyCode), deposit).toWallet()
    )

    val trades = mutableListOf<Trade>()

    val closedOrders = mutableListOf<Order>()

    // Internally used account to store the state
    private val account = InternalAccount(baseCurrency)

    // Logger to use
    private val logger = Logging.getLogger(SimBroker::class)

    private var nextOrderId = 0


    init {
        this.reset()
    }


    /**
     * Update the portfolio with the provided [position] and return the realized PNL as a consequence of this position
     * change.
     */
    private fun updatePosition(position: Position): Amount {
        val asset = position.asset
        val p = account.positions
        val currentPos = p.getOrDefault(asset, Position.empty(asset))
        val newPosition = currentPos + position
        if (newPosition.closed) p.remove(asset) else p[asset] = newPosition
        return currentPos.realizedPNL(position)
    }

    /**
     * Update the account based on an execution. This will perform the following steps:
     *
     * 1. Update cash positions
     * 2. Update the portfolio position for the underlying asset
     * 3. Create and add a trade object to the account
     */
    private fun updateAccount(
        execution: Execution,
        time: Instant
    ) {
        val asset = execution.order.asset
        val position = Position(asset, execution.size, execution.price)

        // Calculate the fees that apply to this execution
        val fee = feeModel.calculate(execution, time, trades)

        // PNL includes the fee
        val pnl = updatePosition(position) - fee
        val newTrade = Trade(
            time,
            asset,
            execution.size,
            execution.price,
            fee,
            pnl.value,
            execution.order.id
        )

        trades.add(newTrade)
        account.cash.withdraw(newTrade.totalCost)
    }

    /**
     * Return the realized PNL of the trades, optionally filter by one or more asset.
     */
    fun realizedPNL(vararg assets: Asset): Wallet {
        return trades.filter { assets.isEmpty() || it.asset in assets }.realizedPNL
    }

    private fun simulateMarket(event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        val time = event.time
        for (order in account.orders.toList()) {
            if (! order.isValid(time)) {
                // todo
            }
            val price = event.getPrice(order.asset)
            if (price != null) {
                if (order.buy and (price <= order.limit)) {
                    // todo
                } else if (order.buy and (price <= order.limit)) {
                    // todo
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
        trades.clear()
        closedOrders.clear()
    }

}

