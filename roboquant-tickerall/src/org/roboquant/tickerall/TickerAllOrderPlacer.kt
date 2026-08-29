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

    private val logger = Logging.getLogger(TickerAllOrderPlacer::class)

    // Lazily-loaded broker symbol specs (lot step / min), keyed by symbol, for quantizing volumes.
    private var symbolSpecs: Map<String, SymbolSpecDTO>? = null

    /**
     * Place [order] at TickerAll. A market order that opposes the current net position is net-emulated (see
     * the class doc); otherwise the order is placed straight through. Returns true when the order was placed
     * as a raw market/limit order (so the caller should track it in the order book) and false when it was
     * net-emulated (closes / reversal, with no resting order to track).
     */
    fun place(order: Order): Boolean {
        if (netEmulateIfOpposing(order)) return false
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
        val body = if (order.limit.isNaN()) {
            PlaceOrderBody(type = "market", symbol = order.asset.symbol, side = side, volume = volume)
        } else {
            PlaceOrderBody(type = "limit", symbol = order.asset.symbol, side = side, volume = volume, price = order.limit)
        }
        val ack = client.placeOrder(body)
        order.id = ack.ticket?.toString()
            ?: throw UnsupportedException("no ticket returned by TickerAll for order=$order")
    }

    /**
     * If [order] is a MARKET order that opposes the current net position for its symbol, emulate netting by
     * closing the net-side tickets up to the order size (plus a market remainder if it reverses past flat)
     * and return true. Otherwise place nothing and return false, so the caller places it raw. A limit
     * opposer returns false: it cannot be a market close and nets when it fills.
     */
    private fun netEmulateIfOpposing(order: Order): Boolean {
        if (!order.limit.isNaN()) return false // only a market order can stand in for a close
        val symbol = order.asset.symbol
        val tickets = client.getAccount().positions.orEmpty()
            .filter { it.symbol == symbol && it.volume != null && it.ticket != null }
        val net = netVolume(tickets)
        val opposing = (net.signum() > 0 && order.sell) || (net.signum() < 0 && order.buy)
        if (!opposing) return false

        val netLong = net.signum() > 0
        var remaining = order.size.toBigDecimal().abs()
        // Close only the net-side tickets, oldest first, partial-closing the last one if the order is smaller.
        val netSide = tickets.filter { isSell(it.side) != netLong }
            .sortedWith(compareBy({ it.openTime ?: "" }, { it.ticket ?: 0L }))
        for (t in netSide) {
            if (remaining.signum() <= 0) break
            val vol = BigDecimal.valueOf(t.volume!!)
            val take = quantize(symbol, vol.min(remaining))
            if (take.signum() <= 0) continue
            val ticket = t.ticket!!.toString()
            if (take >= vol) client.closePosition(ticket) else client.closePosition(ticket, take.toDouble())
            remaining = remaining.subtract(take)
        }

        val leftover = quantize(symbol, remaining)
        if (leftover.signum() > 0) {
            // Reversal past flat: open a fresh position for the leftover in the order's direction.
            client.placeOrder(
                PlaceOrderBody(
                    type = "market",
                    symbol = symbol,
                    side = if (order.buy) "BUY" else "SELL",
                    volume = leftover.toDouble(),
                )
            )
        }
        logger.info { "net-emulated order symbol=$symbol size=${order.size} netBefore=$net" }
        return true
    }

    /** Net signed lot volume across [tickets] (BUY adds, SELL subtracts). */
    private fun netVolume(tickets: List<PositionDTO>): BigDecimal {
        var net = BigDecimal.ZERO
        for (t in tickets) {
            val v = BigDecimal.valueOf(t.volume ?: 0.0)
            net = if (isSell(t.side)) net.subtract(v) else net.add(v)
        }
        return net
    }

    /** Round [vol] DOWN to the symbol's lot step; return 0 if it then falls below the minimum lot. */
    private fun quantize(symbol: String, vol: BigDecimal): BigDecimal {
        if (vol.signum() <= 0) return BigDecimal.ZERO
        val spec = spec(symbol) ?: return vol
        val step = spec.volumeStep?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
        val vmin = spec.volumeMin?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
        val q = if (step.signum() > 0) vol.divideToIntegralValue(step).multiply(step) else vol
        return if (vmin.signum() > 0 && q < vmin) BigDecimal.ZERO else q
    }

    private fun spec(symbol: String): SymbolSpecDTO? {
        if (symbolSpecs == null) {
            symbolSpecs = try {
                client.getSymbolSpecs().filter { it.name != null }.associateBy { it.name!! }
            } catch (e: Exception) {
                logger.warn { "failed to load symbol specs: ${e.message}" }
                emptyMap()
            }
        }
        return symbolSpecs?.get(symbol)
    }

    private fun isSell(side: String?): Boolean = side?.equals("SELL", ignoreCase = true) == true
}
