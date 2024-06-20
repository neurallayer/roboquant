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

package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.Trade
import org.roboquant.brokers.marketValue
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Internal Account is meant to be used by broker implementations, like the SimBroker. The broker is the only one with
 * a reference to the InternalAccount and will communicate the state to the outside world (Policy and Metrics) using
 * the immutable [Account] object.
 *
 * The Internal Account is designed to eliminate common mistakes, but is completely optional to use. The brokers that
 * come with roboquant use this class under the hood, but that is not a requirement for third party integrations.
 *
 * @property baseCurrency The base currency to use for things like reporting
 * @property retention The time to retain trades and closed orders, default is 1 year. Setting this value to a shorter
 * time-span reduces memory usage and speeds up large back tests.
 *
 * @constructor Create a new instance of InternalAccount
 */
class InternalAccount(var baseCurrency: Currency, private val retention: TimeSpan = 1.years) {

    /**
     * When was the account last updated, default if not set is [Instant.MIN]
     */
    var lastUpdate: Instant = Instant.MIN

    /**
     * The trades that have been executed. Trades are only retained based on the [retention] setting.
     */
    internal val trades = mutableListOf<Trade>()

    /**
     * Open orders
     */
    val openOrders = mutableMapOf<String, Order>()

    /**
     * Closed orders. It is private and the only way it gets filled is via the [updateOrder] when the order status is
     * closed. ClosedOrders are only retained based on the [retention] setting.
     */
    val closedOrders = mutableListOf<Order>()

    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    val cash: Wallet = Wallet()

    /**
     * Remaining buying power of the account denoted in the [InternalAccount.baseCurrency] of the account.
     */
    var buyingPower: Amount = Amount(baseCurrency, 0.0)

    /**
     * Portfolio with its open positions. Positions are removed as soon as they are closed
     */
    val portfolio = mutableMapOf<Asset, Position>()

    /**
     * Clear all the state in this account.
     */
    @Synchronized
    fun clear() {
        closedOrders.clear()
        trades.clear()
        lastUpdate = Instant.MIN
        openOrders.clear()
        portfolio.clear()
        cash.clear()
    }

    /**
     * Load the state from an account
     */
    @Synchronized
    fun load(account: Account) {
        clear()
        buyingPower = account.buyingPower
        cash.deposit(account.cash)
        for (p in account.positions) portfolio[p.asset] = p
        for (o in account.openOrders) openOrders[o.id] = o
        closedOrders.addAll(account.closedOrders)
        trades.addAll(account.trades)
        lastUpdate = account.lastUpdate
        baseCurrency = account.baseCurrency
    }

    /**
     * Set the [position] a portfolio. If the position is closed, it is removed all together from the [portfolio].
     */
    @Synchronized
    fun setPosition(position: Position) {
        if (position.closed) {
            portfolio.remove(position.asset)
        } else {
            portfolio[position.asset] = position
        }
    }

    /**
     * Get the open orders
     */
    val orders: List<Order>
        get() = openOrders.values.toList()

    /**
     * Add [orders] as initial orders. This is the first step a broker should take before further processing
     * the orders. Future updates using the [updateOrder] method will fail if there is no known order already present.
     */
    @Synchronized
    fun initializeOrders(orders: Collection<Order>) {
        orders.forEach { openOrders[it.id] = it }
    }



    /**
     * Add a new [trade] to this internal account
     */
    @Synchronized
    fun addTrade(trade: Trade) {
        trades.add(trade)
    }

    /**
     * Update the open positions in the portfolio with the current market prices as found in the [event]
     */
    fun updateMarketPrices(event: Event, priceType: String = "DEFAULT") {
        if (portfolio.isEmpty()) return

        val prices = event.prices
        for ((asset, position) in portfolio) {
            val priceAction = prices[asset]
            if (priceAction != null) {
                val price = priceAction.getPrice(priceType)
                val newPosition = position.copy(mktPrice = price, lastUpdate = event.time)
                portfolio[asset] = newPosition
            }
        }
    }

    /**
     * Closed orders and past trades can grow to larger collections in long back tests. This consumes a lot of memory
     * and makes the back tests slower since at each step this collection is copied to the immutable account object.
     *
     * The retention allows to keep only recent closed orderds and trades in memory, older ones will be discarded. This
     * saves memory and speed-up back tests.
     */
    private fun enforceRetention() {
        if (retention.isZero) {
            trades.clear()
            closedOrders.clear()
            return
        }
        if (lastUpdate > Timeframe.MIN) {
            val earliest = lastUpdate - retention
            while (trades.isNotEmpty() && trades.first().time < earliest) {
                trades.removeFirst()
            }

            // while (closedOrders.isNotEmpty() && closedOrders.first().closedAt < earliest) {
            //    closedOrders.removeFirst()
            // }
        }
    }


    /**
     * Create an immutable [Account] instance that can be shared with other components (Policy and Metric) and is
     * guaranteed not to change after it has been created.
     */
    @Synchronized
    fun toAccount(): Account {
        for (order in openOrders.values.toList()) {
            if (order.status.closed) {
                closedOrders.add(order)
                openOrders.remove(order.id)
            }
        }

        enforceRetention()

        return Account(
            baseCurrency,
            lastUpdate,
            cash.clone(),
            trades.toList(),
            openOrders.values.toList(),
            closedOrders.toList(),
            portfolio.values.toList(),
            buyingPower
        )
    }

    /**
     * Return the total market value for this portfolio
     */
    val marketValue: Wallet
        get() {
            return portfolio.values.marketValue
        }



    /**
     * Get an open order with the provided [orderId], or null if not found
     */
    fun getOrder(orderId: String): Order? = openOrders[orderId]

    fun updateOrder(order: Order, now: Instant?, status: OrderStatus) {
        order.status = status

    }

}

