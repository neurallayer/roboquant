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
 * a reference to the InternalAccount and will communicate the state to the outside world (Trader and Metrics) using
 * the [Account] object.
 *
 * @property baseCurrency The base currency to use for things like reporting
 * time-span reduces memory usage and speeds up large back tests.
 *
 * @constructor Create a new instance of InternalAccount
 */
class InternalAccount(override var baseCurrency: Currency) : Account {

    /**
     * When was the account last updated, default if not set is [Instant.MIN]
     */
    override var lastUpdate: Instant = Instant.MIN

    /**
     * The trades that have been executed.
     */
    override val trades = mutableListOf<Trade>()

    /**
     * Open orders
     */
    override val openOrders = mutableListOf<Order>()

    /**
     * Closed orders. It is private and the only way it gets filled is via the [updateOrder] when the order status is
     * closed.
     */
    override val closedOrders = mutableListOf<Order>()

    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    override val cash: Wallet = Wallet()

    /**
     * Remaining buying power of the account denoted in the [InternalAccount.baseCurrency] of the account.
     */
    override var buyingPower: Amount = Amount(baseCurrency, 0.0)

    /**
     * Portfolio with its open positions. Positions are removed as soon as they are closed
     */
    override val positions = mutableMapOf<Asset, Position>()

    /**
     * Clear all the state in this account.
     */
    @Synchronized
    fun clear() {
        closedOrders.clear()
        trades.clear()
        lastUpdate = Instant.MIN
        openOrders.clear()
        positions.clear()
        cash.clear()
    }


    /**
     * Set the [position]. If the position is closed, it is removed all together from the [positions].
     */
    @Synchronized
    fun setPosition(position: Position) {
        if (position.closed) {
            positions.remove(position.asset)
        } else {
            positions[position.asset] = position
        }
    }


    /**
     * Add [orders] as initial orders. This is the first step a broker should take before further processing
     * the orders. Future updates using the [updateOrder] method will fail if there is no known order already present.
     */
    @Synchronized
    fun initializeOrders(orders: Collection<Order>) {
        openOrders.addAll(orders)
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
        if (positions.isEmpty()) return

        val prices = event.prices
        for ((asset, position) in positions) {
            val priceItem = prices[asset]
            if (priceItem != null) {
                val price = priceItem.getPrice(priceType)
                val newPosition = position.copy(mktPrice = price, lastUpdate = event.time)
                positions[asset] = newPosition
            }
        }
    }

    /**
     * Create an immutable [Account] instance that can be shared with other components (Trader and Metric) and is
     * guaranteed not to change after it has been created.
     */
    @Synchronized
    fun toAccount(): Account {
        val newlyClosed = openOrders.filter { it.closed }.toSet()
        closedOrders.addAll(newlyClosed)
        openOrders.removeAll(newlyClosed)
        return this
    }

    /**
     * Return the total market value for this portfolio
     */
    val marketValue: Wallet
        get() {
            return positions.values.marketValue
        }


    /**
     * Get an open order with the provided [orderId], or null if not found
     */
    fun getOpenOrder(orderId: String): Order? = openOrders.find { it.id == orderId }

    fun updateOrder(order: Order, now: Instant?, status: OrderStatus) {
        order.status = status
        if (now != null) {
            if (order.openedAt == Timeframe.MIN) order.openedAt = now
        }

    }


    override fun toString(): String {

        val positionString  = positions.values.joinToString(limit = 20) { "${it.size}@${it.asset.symbol}" }

        return """
            last update  : $lastUpdate
            base currency: $baseCurrency
            cash         : $cash
            buying Power : $buyingPower
            equity       : ${equity()}
            positions    : ${positionString.ifEmpty { "-" }}
            open orders  : ${openOrders.size}
            closed orders: ${closedOrders.size}
            trades       : ${trades.size}
        """.trimIndent()

    }

}

