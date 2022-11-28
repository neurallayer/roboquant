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

package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.Account
import org.roboquant.brokers.Position
import org.roboquant.brokers.Trade
import org.roboquant.brokers.marketValue
import org.roboquant.common.*
import org.roboquant.common.Config.baseCurrency
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Internal Account is meant to be used by broker implementations, like the SimBroker. The broker is the only one with a
 * reference to the InternalAccount and will communicate to the outside world (Policy and Metrics) only the
 * immutable [Account] version.
 *
 * The Internal Account is designed to eliminate common mistakes, but is completely optional. The broker that come with
 * roboquant all use this under the hood, but this is no hard requirement.
 *
 * @property baseCurrency The base currency to use for things like reporting
 * @constructor Create a new Account
 */
class InternalAccount(var baseCurrency: Currency = Config.baseCurrency) {

    /**
     * When was the account last updated, default if not set is [Instant.MIN]
     */
    var lastUpdate: Instant = Instant.MIN

    /**
     * The trades that have been executed
     */
    val trades = mutableListOf<Trade>()

    /**
     * Open orders
     */
    private val openOrders = mutableMapOf<Int, OrderState>()

    /**
     * Closed orders. It is private and the only way it gets filled is via the [updateOrder] when the order status is
     * closed
     */
    private val closedOrders = mutableListOf<OrderState>()

    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    val cash: Wallet = Wallet()

    /**
     * Remaining buying power of the account denoted in the [baseCurrency] of the account.
     */
    var buyingPower: Amount = Amount(baseCurrency, 0.0)

    /**
     * Portfolio with its positions
     */
    val portfolio = mutableMapOf<Asset, Position>()

    /**
     * Clear all the state in this account.
     */
    fun clear() {
        lastUpdate = Instant.MIN
        trades.clear()
        openOrders.clear()
        closedOrders.clear()
        portfolio.clear()
        cash.clear()
    }


    /**
     * Set the position in a portfolio. If the position is closed, it is removed from the portfolio.
     */
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
    val orders: List<OrderState>
        get() = openOrders.values.toList()


    /**
     * Add [orders] as initial orders. This is the first step a broker should take before further processing
     * the orders. Future updates using the [updateOrder] method will fail if there is no known order already present.
     */
    fun initializeOrders(orders: Collection<Order>) {
        val newOrders = orders.map { OrderState(it) }
        newOrders.forEach { openOrders[it.orderId] = it }
    }


    /**
     * Update an [order] with a new [status] at a certain time. This only successful if order has been already added
     * before. When an order reaches a close state it will be moved internally to a different store and no
     * longer be directly accessible.
     */
    fun updateOrder(order: Order, time: Instant, status: OrderStatus) {
        val id = order.id
        val state = openOrders.getValue(id)
        val newState = state.update(status, time)
        if (newState.open) {
            openOrders[id] = newState
        } else {
            // order is closed, so remove it from the open orders
            openOrders.remove(id) ?: throw UnsupportedException("cannot close an order that was not open first")
            closedOrders.add(newState)
        }
    }

    /**
     * Add a new [trade] to this internal account
     */
    fun addTrade(trade: Trade) {
        trades.add(trade)
    }

    /**
     * Update the open positions in the portfolio with the current market prices as found in the [event]
     */
    fun updateMarketPrices(event: Event, priceType: String = "DEFAULT") {
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
     * Create an immutable [Account] instance that can be shared with other components (Policy and Metric) and is
     * guaranteed not to change after it has been created.
     */
    fun toAccount(): Account {
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
     * Reject an [order] at the provided [time]
     */
    fun rejectOrder(order: Order, time: Instant) {
        updateOrder(order, time, OrderStatus.REJECTED)
    }

    /**
     * Accept an [order] at the provided [time]
     */
    fun acceptOrder(order: Order, time: Instant) {
        updateOrder(order, time, OrderStatus.ACCEPTED)
    }

    /**
     * Complete an [order] at the provided [time]
     */
    fun completeOrder(order: Order, time: Instant) {
        updateOrder(order, time, OrderStatus.COMPLETED)
    }

    /**
     * Get an open order with the provided [orderId]
     */
    fun getOrder(orderId: Int): Order? = openOrders[orderId]?.order

}

