/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import org.roboquant.brokers.sim.execution.ExecutionEngine
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.orders.createCancelOrders
import java.time.Instant

/**
 * Simulated Broker that is used as the broker during back testing and live testing. It simulates both broker and
 * exchange behavior. It can be configured with various plug-ins that determine its exact behavior:
 *
 * @property initialDeposit initial deposit, default is 1 million USD
 * @param baseCurrency the base currency to use for reporting values, default is the (first) currency found in the
 * initial deposit
 * @property feeModel the fee/commission model to use, default is [NoFeeModel]
 * @property accountModel the account model (like cash or margin) to use, default is [CashAccount]
 * @param pricingEngine the pricing engine to use to simulate trades, default is [SpreadPricingEngine]
 * @constructor Create new instance of SimBroker
 */
class SimBroker(
    private val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val feeModel: FeeModel = NoFeeModel(),
    private val accountModel: AccountModel = CashAccount(),
    pricingEngine: PricingEngine = SpreadPricingEngine(),
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] in a [currencyCode]
     */
    constructor(deposit: Number, currencyCode: String = "USD") : this(
        Amount(
            Currency.getInstance(currencyCode),
            deposit
        ).toWallet()
    )

    // Internally used account to store the state
    private val _account = InternalAccount(baseCurrency)

    // Logger to use
    private val logger = Logging.getLogger(SimBroker::class)

    // Execution engine for simulating trades
    private val executionEngine = ExecutionEngine(pricingEngine)

    /**
     * Get the latest state of the account
     */
    override val account: Account
        get() = _account.toAccount()

    init {
        reset()
    }

    /**
     * Update the portfolio with the provided [position] and return the realized PNL.
     */
    private fun updatePosition(position: Position): Amount {
        val asset = position.asset
        val p = _account.portfolio
        val currentPos = p.getOrDefault(asset, Position.empty(asset))
        val newPosition = currentPos + position
        if (newPosition.closed) p.remove(asset) else p[asset] = newPosition
        return currentPos.realizedPNL(position)
    }

    /**
     * Update the account based on an execution. This will perform the following steps:
     *
     * 1. Update the cash position
     * 2. Update the portfolio position for the underlying asset
     * 3. Create and add a trade object to the account
     *
     */
    private fun updateAccount(
        execution: Execution,
        time: Instant
    ) {

        val asset = execution.order.asset
        val position = Position(asset, execution.size, execution.price)
        val fee = feeModel.calculate(execution)

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

        _account.addTrade(newTrade)
        _account.cash.withdraw(newTrade.totalCost)
    }

    private fun simulateMarket(newOrders: List<Order>, event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        executionEngine.addAll(newOrders)
        val executions = executionEngine.execute(event)

        // Process the new executions
        for (execution in executions) updateAccount(execution, event.time)

        // Get the latest state of orders
        executionEngine.orderStates.forEach {
            _account.updateOrder(it.first, event.time, it.second)
        }

        executionEngine.removeClosedOrders()
    }

    /**
     * Place [orders] at this broker and provide the [event] that just occurred. The event is just by the SimBroker to
     * get the prices required to simulate the trading on an exchange. Return an updated [Account] instance
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.trace { "Received ${orders.size} orders at ${event.time}" }
        _account.initializeOrders(orders)
        simulateMarket(orders, event)
        _account.updateMarketPrices(event)
        _account.lastUpdate = event.time
        accountModel.updateAccount(_account)
        return account
    }

    /**
     * Close the open positions and return the updated account. This comes in handy at the end of a back-test if you
     * don't want to have open positions left in the portfolio.
     *
     * This method performs the following two steps:
     * 1. cancel open orders
     * 2. close open positions by creating and processing [MarketOrder] for the required quantities, using the
     * last known market price for an asset as the price action.
     */
    fun closePositions(time: Instant = _account.lastUpdate): Account {
        val account = account
        val cancelOrders = account.openOrders.createCancelOrders()
        val change = account.positions.closeSizes
        val changeOrders = change.map { MarketOrder(it.key, it.value) }
        val actions = account.positions.map { TradePrice(it.asset, it.mktPrice) }
        val event = Event(actions, time)
        return place(cancelOrders + changeOrders, event)
    }

    /**
     * Reset all the state and set the cash balance to the [initialDeposit].
     */
    override fun reset() {
        _account.clear()
        executionEngine.clear()
        _account.cash.deposit(initialDeposit)
        accountModel.updateAccount(_account)
    }

}

