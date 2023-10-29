/*
 * Copyright 2020-2023 Neural Layer
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

import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.Position
import org.roboquant.brokers.Trade
import org.roboquant.brokers.sim.execution.Execution
import org.roboquant.brokers.sim.execution.ExecutionEngine
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
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
 * @param pricingEngine the pricing engine to use to calculate trade pricing, default is [SpreadPricingEngine]
 * @constructor Create a new instance of SimBroker
 */
open class SimBroker(
    val initialDeposit: Wallet = Wallet(1_000_000.00.USD),
    baseCurrency: Currency = initialDeposit.currencies.first(),
    private val feeModel: FeeModel = NoFeeModel(),
    private val accountModel: AccountModel = CashAccount(),
    pricingEngine: PricingEngine = SpreadPricingEngine(),
    retention: TimeSpan = 1.years
) : Broker {

    /**
     * Create a new SimBroker with a [deposit] denoted in a single [currencyCode].
     */
    constructor(deposit: Number, currencyCode: String = "USD") : this(
        Amount(Currency.getInstance(currencyCode), deposit).toWallet()
    )

    // Internally used account to store the state
    private val _account = InternalAccount(baseCurrency, retention)

    // Logger to use
    private val logger = Logging.getLogger(SimBroker::class)

    // Execution engine used for simulating trades
    private val executionEngine = ExecutionEngine(pricingEngine)

    /**
     * Get the state of the account since the last [sync]
     */
    override var account: Account = _account.toAccount()


    init {
        this.reset()
    }

    /**
     * Update the portfolio with the provided [position] and return the realized PNL as a consequence of this position
     * change.
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
     * 1. Update cash positions
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

        // Calculate the fees that apply to this execution
        val fee = feeModel.calculate(execution, time, this.account.trades)

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

    /**
     * Invoke the [executionEngine] to simulate the execution of open orders and update the internal account afterwards
     * with any trades made.
     */
    private fun simulateMarket(event: Event) {
        // Add new orders to the execution engine and run it with the latest events
        val executions = executionEngine.execute(event)

        // Process any new executions to be reflected in the trades and cash balance
        for (execution in executions) updateAccount(execution, event.time)

        // Get the latest state of orders and update the status of internal account
        executionEngine.orderStates.forEach {
            _account.updateOrder(it.first, event.time, it.second)
        }

        // Now it is safe to remove closed orders from the execution engine
        executionEngine.removeClosedOrders()
    }

    /**
     * Run the simulation given the provided [event].
     */
    override fun sync(event: Event) {
        simulateMarket(event)
        _account.updateMarketPrices(event)
        _account.lastUpdate = event.time
        accountModel.updateAccount(_account)
        account = _account.toAccount()
    }

    /**
     * Place the [orders] at the broker.
     */
    override fun place(orders: List<Order>, time: Instant) {
        logger.trace { "Received orders=${orders.size} time=$time" }
        _account.initializeOrders(orders)
        executionEngine.addAll(orders)
    }

    /**
     * Reset all the state and set the cash balance back to the [initialDeposit].
     */
    override fun reset() {
        _account.clear()
        executionEngine.clear()
        _account.cash.deposit(initialDeposit)
        accountModel.updateAccount(_account)
        account = _account.toAccount()
    }

}

