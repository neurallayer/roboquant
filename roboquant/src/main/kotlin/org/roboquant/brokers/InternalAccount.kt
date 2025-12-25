/*
 * Copyright 2020-2026 Neural Layer
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
import org.roboquant.common.Event
import java.time.Instant
import kotlin.collections.iterator

/**
 * Internal Account is meant to be used by broker implementations, like the SimBroker. The broker is the only one with
 * a reference to the InternalAccount and will communicate the state to the outside world (SignalConverter and Metrics) using
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
     * Open orders
     */
    override val orders = mutableListOf<Order>()


    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    override val cash: Wallet = Wallet()

    /**
     * Remaining buying power of the account denoted in the [InternalAccount.baseCurrency] of the account.
     */
    override var buyingPower: Amount = Amount(baseCurrency, 0.0)

    /**
     * Trades that happened to far
     */
    override val trades = mutableListOf<Trade>()

    /**
     * Portfolio with its open positions. Positions are removed as soon as they are closed
     */
    override val positions = mutableMapOf<Asset, Position>()

    /**
     * Clear all the state in this account.
     */
    @Synchronized
    fun clear() {
        lastUpdate = Instant.MIN
        orders.clear()
        positions.clear()
        cash.clear()
    }

    /**
     * Delete orders with the same id as the one provided.
     */
    fun deleteOrder(order: Order) {
        orders.removeIf { it.id == order.id }
    }



    /**
     * Set the [position]. If the position is closed, it is removed all together from the [positions].
     */
    @Synchronized
    fun setPosition(asset: Asset, position: Position) {
        if (position.closed) {
            positions.remove(asset)
        } else {
            positions[asset] = position
        }
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
     * Create an [Account] instance
     */
    @Synchronized
    fun toAccount(): Account {
        return this
    }

    /**
     * Short overview of account state
     */
    override fun toString(): String {
        val pString = positions.map { it.value.size.toString() + "@" + it.key.symbol }.joinToString(separator = ", ")
        val oString = orders.joinToString(separator = ", ") { it.size.toString() + "@" + it.asset.symbol }

        return """
            last update  : $lastUpdate
            cash         : $cash
            buying Power : $buyingPower
            equity       : ${equity()}
            positions    : $pString
            open orders  : $oString
            trades       : ${trades.size}
        """.trimIndent()

    }

}

