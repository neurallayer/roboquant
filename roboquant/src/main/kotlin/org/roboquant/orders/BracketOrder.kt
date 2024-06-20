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

package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Size
import org.roboquant.common.percent

/**
 * A bracket order enables you to place an order and at the same time place orders to take profit and limit the loss.
 * All three suborders require having the same underlying asset.
 * Additionally, the size of the [takeProfit] and [stopLoss] orders should be opposite of the [entry] order.
 *
 * Although the SimBroker is very flexible and supports any type of single order, real brokers often are more limited.
 * So it is advised to restrict your bracket orders to the following subsets if you plan to go live:
 *
 * - The entry order - either a Market or Limit order
 * - The takeProfit order - a Limit order
 * - The stopLoss order - a StopLoss or StopLimit order
 *
 * @property entry the entry order
 * @property takeProfit the take profit order
 * @property stopLoss the stop loss order
 * @param tag and optional tag, default is an empty string
 * @constructor create a new instance of a BracketOrder
 */
class BracketOrder(
    val entry: SingleOrder,
    val takeProfit: SingleOrder,
    val stopLoss: SingleOrder,
    tag: String = ""
) : Order(entry.asset, tag) {

    init {
        require(entry.asset == takeProfit.asset && entry.asset == stopLoss.asset) {
            "bracket orders can only contain orders for the same asset"
        }
        require(entry.size == -takeProfit.size && entry.size == -stopLoss.size) {
            "takeProfit and stopLoss orders need to have opposite sizes of the entry order"
        }
    }

    override fun info() = sortedMapOf("entry" to entry, "takeProfit" to takeProfit, "stopLoss" to stopLoss)

    /**
     * Common bracket-orders, making it easier to create one without the risk of introducing mistakes in the sizing and
     * limits of the underlying suborders.
     */
    companion object {

        /**
         * Create a bracket order meeting the following criteria:
         * - the entry-order is a [MarketOrder] for the provided [asset] and [size]
         * - the take-profit order is a [TrailOrder] with the specified [trailPercentage]
         * - the stop-loss order is a [StopOrder] using a stop priced based on the provided [stopPercentage]
         */
        fun marketTrailStop(
            asset: Asset,
            size: Size,
            price: Double,
            trailPercentage: Double = 5.percent, // 5%
            stopPercentage: Double = 1.percent // 1%
        ): BracketOrder {
            require(stopPercentage > 0.0) { "stopPercentage should be a positive value, for example 0.05 for 5%" }
            val stopPrice = price * (1.0 - (size.sign * stopPercentage))
            return BracketOrder(
                MarketOrder(asset, size),
                TrailOrder(asset, -size, trailPercentage),
                StopOrder(asset, -size, stopPrice)
            )
        }

        /**
         * Create a bracket order meeting the following criteria:
         * - the entry-order is a [LimitOrder] with the provided [limitPrice], [asset] and [size]
         * - the take-profit order is a [TrailOrder] with the specified [trailPercentage]
         * - the stop-loss order is a [StopOrder] using a stop priced based on the provided [stopPercentage] relative
         * to the limitPrice.
         */
        @Suppress("LongParameterList")
        fun limitTrailStop(
            asset: Asset,
            size: Size,
            limitPrice: Double,
            trailPercentage: Double = 5.percent, // 5%
            stopPercentage: Double = 1.percent // 1%
        ): BracketOrder {
            require(stopPercentage > 0.0) { "stopPercentage should be a positive value, for example 0.05 for 5%" }
            val stopPrice = limitPrice * (1.0 - (size.sign * stopPercentage))
            return BracketOrder(
                LimitOrder(asset, size, limitPrice),
                TrailOrder(asset, -size, trailPercentage),
                StopOrder(asset, -size, stopPrice)
            )
        }

    }
}
