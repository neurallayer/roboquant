/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderSide
import net.jacobpeterson.alpaca.model.endpoint.orders.enums.OrderTimeInForce
import net.jacobpeterson.alpaca.rest.AlpacaClientException
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.common.UnsupportedException
import org.roboquant.orders.*

internal class AlpaceOrderPlacer(private val alpacaAPI: AlpacaAPI, private val extendedHours: Boolean = false) {

    private val orders = mutableMapOf<Order, String>()
    private val logger = Logging.getLogger(AlpacaBroker::class)


    fun cancelOrder(cancellation: CancelOrder): Boolean {
        return try {
            val orderId = orders[cancellation.order]
            alpacaAPI.orders().cancel(orderId)
            true
        } catch (exception: AlpacaClientException) {
            logger.trace(exception) { "cancellation failed for order=$cancellation" }
            false
        }
    }

    fun get(order: Order) = orders[order]

    fun findByAlapacaId(orderId: String) = orders.filterValues { it == orderId }.keys.firstOrNull()

    fun placeBracketOrder(order: BracketOrder) {

        val api = alpacaAPI.orders()
        val side = if (order.entry.buy) OrderSide.BUY else OrderSide.SELL
        val entry = order.entry as MarketOrder
        val tp = order.takeProfit as LimitOrder
        val sl = order.stopLoss as StopLimitOrder

        val tif = when (entry.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> throw UnsupportedException("unsupported tif=${entry.tif} for order=$order")
        }

        require(!entry.size.isFractional) { "fractional orders are not supported for bracket orders" }
        val qty = entry.size.toBigDecimal().abs().toInt()

        val alpacaOrder = api.requestMarketBracketOrder(
            entry.asset.symbol,
            qty,
            side,
            tif,
            tp.limit,
            sl.stop,
            sl.limit
        )

        orders[order] = alpacaOrder.id
    }

    /**
     * Place an order of type [SingleOrder] at Alpaca.
     *
     * @param order
     */
    fun placeSingleOrder(order: SingleOrder) {
        val asset = order.asset
        require(asset.type in setOf(AssetType.STOCK, AssetType.CRYPTO)) {
            "only stocks and crypto supported, received ${asset.type}"
        }

        val tif = when (order.tif) {
            is GTC -> OrderTimeInForce.GOOD_UNTIL_CANCELLED
            is DAY -> OrderTimeInForce.DAY
            else -> throw UnsupportedException("unsupported tif=${order.tif} for order=$order")
        }

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        require(!order.size.isFractional || order is LimitOrder) {
            "fractional orders only supported for limit orders"
        }

        val qty = order.size.toBigDecimal().abs()
        val api = alpacaAPI.orders()

        val alpacaOrder = when (order) {
            is MarketOrder -> api.requestMarketOrder(asset.symbol, qty.toInt(), side, tif)
            is LimitOrder -> api.requestLimitOrder(asset.symbol, qty.toDouble(), side, tif, order.limit, extendedHours)
            is StopOrder -> api.requestStopOrder(asset.symbol, qty.toInt(), side, tif, order.stop, extendedHours)
            is StopLimitOrder -> api.requestStopLimitOrder(
                asset.symbol, qty.toInt(), side, tif, order.limit, order.stop, extendedHours
            )

            is TrailOrder -> api.requestTrailingStopPercentOrder(
                asset.symbol, qty.toInt(), side, tif, order.trailPercentage, extendedHours
            )

            else -> throw UnsupportedException("unsupported single order type order=$order")
        }
        orders[order] = alpacaOrder.id
    }

    /**
     * Add an order that already existed when starting the broker implementation
     */
    fun addExistingOrder(rqOrder: Order, id: String) {
        orders[rqOrder] = id
    }


}
