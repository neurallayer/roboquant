/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.brokers

import org.roboquant.common.*
import org.roboquant.common.Config.baseCurrency
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Internal Account is only used by broker implementations, like the SimBroker. The broker is the only one with a
 * reference to this account and will communicate to the outside world (Policy and Metrics) only the immutable [Account]
 * version.
 *
 * - Cash balances in the account
 * - the [portfolio] with its assets
 * - The past [trades]
 * - The [orders], both open (still in progress) and already closed ones
 *
 * It also supports multi-currency trading through the use of a pluggable currency converter. Without configuring such
 * plug-in it still supports single currency trading, so when all assets and cash balances are denoted in a single
 * currency.
 *
 * Only a broker should ever update the account. For other components like a policy, it should be used read-only.
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
    val openOrders = mutableMapOf<Int, OrderState>()

    /**
     * Closed orders
     */
    val closedOrders = mutableListOf<OrderState>()

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
     * Put a single order, replacing existing one with the same order id or otherwise add it.
     */
    fun putOrder(orderState: OrderState) {
        val id = orderState.id
        if (orderState.open) {
            openOrders[id] = orderState
        } else {
            // order is closed, so remove it from the open orders
            openOrders.remove(id)
            closedOrders.add(orderState)
        }
    }

    /**
     * Put orders, replacing existing ones with the same order id or otherwise add them.
     */
    fun putOrders(orderStates: Collection<OrderState>) {
        for (orderState in orderStates) putOrder(orderState)
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
     * Create an immutable [Account] instance that can be shared with other components (Policy and Metric).
     */
    fun toAccount(): Account {
        return Account(
            baseCurrency,
            lastUpdate,
            cash.clone(),
            trades.toList(),
            openOrders.values.toList(),
            closedOrders.toList(),
            portfolio.toMap().withDefault { Position.empty(it) },
            buyingPower
        )
    }

}

/**
 * Reject an order
 *
 * @param order
 * @param time
 */
fun InternalAccount.rejectOrder(order: Order, time: Instant) {
    putOrder(OrderState(order, OrderStatus.REJECTED, time, time))
}

/**
 * Accept an order
 *
 * @param order
 * @param time
 */
fun InternalAccount.acceptOrder(order: Order, time: Instant) {
    putOrder(OrderState(order, OrderStatus.ACCEPTED, time, time))
}

val Collection<Order>.initialOrderState
    get() = map { OrderState(it, OrderStatus.INITIAL) }