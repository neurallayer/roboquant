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
import net.jacobpeterson.alpaca.openapi.trader.model.OrderType
import net.jacobpeterson.alpaca.openapi.trader.model.PostOrderRequest
import net.jacobpeterson.alpaca.openapi.trader.model.TimeInForce

import org.roboquant.common.UnsupportedException
import org.roboquant.orders.*

/**
 * Utility class that translates roboquant orders to alpaca orders
 */
internal class AlpaceOrderPlacer(private val alpacaAPI: AlpacaAPI, private val extendedHours: Boolean = false) {




    private fun getOrderRequest(order: Order): PostOrderRequest? {

        val side = if (order.buy) OrderSide.BUY else OrderSide.SELL

        val qty = order.size.toBigDecimal().abs()
        val result = PostOrderRequest()
            .symbol(order.asset.symbol)
            .side(side)
            .timeInForce(TimeInForce.GTC)
            .qty(qty.toString())
            .extendedHours(extendedHours)


        result.type(OrderType.LIMIT)
        result.limitPrice(order.limit.toString())


        return result
    }

    /**
     * Place an order at Alpaca.
     *
     * @param order
     */
    fun placeSingleOrder(order: Order) {

        val orderRequest = getOrderRequest(order)
        if (orderRequest != null) {
            val alpacaOrder = alpacaAPI.trader().orders().postOrder(orderRequest)
            order.id = alpacaOrder.id!!
        } else {
            throw UnsupportedException("unsupported single order type order=$order")
        }

    }


}
