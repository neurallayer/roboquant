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

package org.roboquant.tickerall

import org.roboquant.common.Order
import org.roboquant.common.UnsupportedException

/**
 * Utility class that translates a roboquant [Order] into a TickerAll REST order request.
 *
 * A roboquant order only carries a signed [Order.size] and a [Order.limit] price, so the mapping is:
 * - a `NaN` limit is sent as a MetaTrader market order,
 * - a finite limit is sent as a pending limit order.
 *
 * This mirrors the Alpaca broker, which likewise only distinguishes market and limit orders. MetaTrader
 * stop orders and stop-loss / take-profit are not expressible in roboquant's order model and are out of
 * scope for this integration.
 */
internal class TickerAllOrderPlacer(private val client: TickerAllClient) {

    /**
     * Place a single [order] at TickerAll and write the broker-assigned ticket back into [Order.id].
     */
    fun placeSingleOrder(order: Order) {
        val side = if (order.buy) "BUY" else "SELL"
        val volume = order.size.toBigDecimal().abs().toDouble()
        val body = if (order.limit.isNaN()) {
            PlaceOrderBody(type = "market", symbol = order.asset.symbol, side = side, volume = volume)
        } else {
            PlaceOrderBody(type = "limit", symbol = order.asset.symbol, side = side, volume = volume, price = order.limit)
        }
        val ack = client.placeOrder(body)
        order.id = ack.ticket?.toString()
            ?: throw UnsupportedException("no ticket returned by TickerAll for order=$order")
    }
}
