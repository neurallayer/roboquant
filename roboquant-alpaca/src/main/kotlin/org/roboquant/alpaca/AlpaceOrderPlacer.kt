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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.openapi.trader.model.OrderSide
import net.jacobpeterson.alpaca.openapi.trader.model.PostOrderRequest
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.common.UnsupportedException
import org.roboquant.orders.*
import java.lang.Exception
import java.util.UUID
import net.jacobpeterson.alpaca.openapi.trader.model.TimeInForce as OrderTimeInForce

/**
 * Utility class that translates roboquant orders to alpaca orders
 */
internal class AlpaceOrderPlacer(private val alpacaAPI: AlpacaAPI, private val extendedHours: Boolean = false) {

    private val orders = mutableMapOf<Order, String>()
    private val logger = Logging.getLogger(AlpacaBroker::class)


    fun cancelOrder(cancellation: CancelOrder): Boolean {
        return try {
            val orderId = orders[cancellation.order]
            alpacaAPI.trader().orders().deleteOrderByOrderID(UUID.fromString(orderId))
            true
        } catch (exception: Exception) {
            logger.trace(exception) { "cancellation failed for order=$cancellation" }
            false
        }
    }

    fun get(order: Order) = orders[order]


    private fun TimeInForce.toOrderTimeInForce(): OrderTimeInForce {

        return when (this) {
            is GTC -> OrderTimeInForce.GTC
            is DAY -> OrderTimeInForce.DAY
            else -> throw UnsupportedException("unsupported tif=${this}")
        }
    }


    private fun getOrderRequest(order: SingleOrder): PostOrderRequest? {

        val tif = order.tif.toOrderTimeInForce()

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL
        require(!order.size.isFractional || order is LimitOrder) {
            "fractional orders only supported for limit orders"
        }

        val qty = order.size.toBigDecimal().abs()
        val result = PostOrderRequest()
            .symbol(order.asset.symbol)
            .side(side)
            .qty(qty.toString())
            .timeInForce(tif)
            .extendedHours(extendedHours)

        if (order is LimitOrder) {
            result.limitPrice(order.limit.toString())
        }
        return result
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

        val orderRequest = getOrderRequest(order)
        if (orderRequest != null) {
            val alpacaOrder = alpacaAPI.trader().orders().postOrder(orderRequest)
            order.id = alpacaOrder.id!!
        } else {
            throw UnsupportedException("unsupported single order type order=$order")
        }

    }

    /**
     * Add an order that already existed when starting the broker implementation
     */
    fun addExistingOrder(rqOrder: Order, id: String) {
        orders[rqOrder] = id
    }


}
