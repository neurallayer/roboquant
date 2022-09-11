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
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.CancelOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import java.time.Instant
import java.util.logging.Logger

/**
 * Simulated Broker that is used during back testing and live testing. It simulates both broker and exchange behavior.
 * It can be configured with various plug-ins that determine it exact behavior:
 *
 * You can provide an [initialDeposit] and base currency
 * @constructor Create new SimBroker instance
 */
class SimBroker(
    private val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val feeModel: FeeModel = NoFeeModel(),
    private val accountModel: AccountModel = CashAccount(),
    pricingEngine: PricingEngine = SpreadPricingEngine(),
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] in currency
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
    private val logger: Logger = Logging.getLogger(SimBroker::class)

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

        _account.trades += newTrade
        _account.cash.withdraw(newTrade.totalCost)
    }

    private fun simulateMarket(newOrders: List<Order>, event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        executionEngine.addAll(newOrders)
        val executions = executionEngine.execute(event)

        // Process the new executions
        for (execution in executions) updateAccount(execution, event.time)

        // Get the latest state of orders
        val orderStates = executionEngine.orderStates
        _account.putOrders(orderStates)
        executionEngine.removeClosedOrders()
    }

    private fun updateBuyingPower() {
        val value = accountModel.getBuyingPower(_account)
        logger.finer { "Calculated buying power $value" }
        _account.buyingPower = value
    }

    /**
     * Place [orders] at this broker and provide the event that just occurred. The event is just by the SimBroker to
     * get the prices required to simulate the trading on an exchange.
     *
     * @param orders The new orders
     * @param event
     */
    override fun place(orders: List<Order>, event: Event): Account {
        logger.finer { "Received ${orders.size} orders at ${event.time}" }
        simulateMarket(orders, event)
        _account.updateMarketPrices(event)
        _account.lastUpdate = event.time
        updateBuyingPower()
        return account
    }

    /**
     * Liquidate the portfolio. This comes in handy at the end of a back-test if you prefer to have no open positions
     * left in the portfolio.
     *
     * This method performs the following two steps:
     * 1. cancel all open orders
     * 2. close all positions by creating and processing [MarketOrder] for the required quantities, using the
     * last known market price for an asset as the price action.
     */
    fun liquidatePortfolio(time: Instant = _account.lastUpdate): Account {
        val cancelOrders = _account.openOrders.map { CancelOrder(it.value) }
        val change = _account.portfolio.diff(emptyList())
        val changeOrders = change.map { MarketOrder(it.key, it.value) }
        val orders = cancelOrders + changeOrders
        val actions = _account.portfolio.values.map { TradePrice(it.asset, it.mktPrice) }
        val event = Event(actions, time)
        return place(orders, event)
    }

    override fun reset() {
        _account.clear()
        executionEngine.clear()
        _account.cash.deposit(initialDeposit)
        updateBuyingPower()
    }

}

