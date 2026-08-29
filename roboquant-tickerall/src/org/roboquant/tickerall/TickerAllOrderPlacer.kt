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

import org.roboquant.common.Logging
import org.roboquant.common.Order
import org.roboquant.common.UnsupportedException
import java.math.BigDecimal

/**
 * Translates a roboquant [Order] into TickerAll REST calls.
 *
 * A roboquant order carries only a signed [Order.size] and an [Order.limit] price, so the mapping is:
 * - a `NaN` limit is a MetaTrader market order, a finite limit is a pending limit order;
 * - roboquant has no explicit close: a close / reduce / reverse is expressed as a new opposite-signed order.
 *
 * On a MetaTrader **netting** account the broker nets an opposite order into the existing position. On a
 * **hedging** account (a separate ticket per trade) a raw opposite order would instead open a SECOND ticket
 * against the first. So a MARKET order that opposes the current net position is *net-emulated*: the net-side
 * tickets are closed (oldest first, partial-closing the last one if needed) up to the order size, and any
 * size beyond flat is opened as a fresh market order (a reversal). This produces the one-net-position-per-
 * asset behaviour roboquant models on either account type. Non-opposing orders and limit opposers are placed
 * unchanged (a limit opposer cannot be a market close and simply nets when it fills).
 *
 * MetaTrader stop orders and stop-loss / take-profit are not expressible in roboquant's order model and are
 * out of scope for this integration.
 */
internal class TickerAllOrderPlacer(private val client: TickerAllClient) {

    /**
     * Place [order] at TickerAll. A market order that opposes the current net position is net-emulated (see
     * the class doc); otherwise the order is placed straight through. Returns true when the order was placed
     * as a raw market/limit order (so the caller should track it in the order book) and false when it was
     * net-emulated (closes / reversal, with no resting order to track).
     */
    fun place(order: Order): Boolean {
        placeSingleOrder(order)
        return true
    }

    /**
     * Place a single [order] straight through as a new market or limit order (the original netting-account
     * behaviour), writing the broker-assigned ticket back into [Order.id].
     */
    fun placeSingleOrder(order: Order) {
        val side = if (order.buy) "BUY" else "SELL"
        val volume = order.size.toBigDecimal().abs().toDouble()
        val body = PlaceOrderBody(type = "limit", symbol = order.asset.symbol, side = side, volume = volume, price = order.limit)
        val ack = client.placeOrder(body)
        order.id = ack.ticket?.toString()
            ?: throw UnsupportedException("no ticket returned by TickerAll for order=$order")
    }

}
