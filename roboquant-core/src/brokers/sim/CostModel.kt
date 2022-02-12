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

package org.roboquant.brokers.sim

import org.roboquant.feeds.PriceAction
import org.roboquant.orders.Order
import kotlin.math.absoluteValue

/**
 * Calculate the price to be used for executing orders and fees that might apply.
 *
 * 1. calculatePrice: raise/lower the price paid due to spread and slippage
 * 2. calculateFee: include a commision/fee in case you simulate a commision based broker
 */
interface CostModel {

    /**
     * Calculate the price to be used for executing an [order] based on the provided [PriceAction]. The returned
     * price should be denoted in the currency of the asset.
     *
     */
    fun calculatePrice(order: Order, priceAction: PriceAction): Double

    /**
     * Any fees and commisions applicable for the [execution]. The returned value should be
     * denoted in the currency of the underlying asset of the order. Typically a fee should be a positive value unless
     * you want to model rebates and other reward structures.
     */
    fun calculateFee(execution: Execution): Double

}

/**
 * Default cost model, using a fixed percentage expressed in basis points and optional a commission fee. This
 * percentage would cover both spread and slippage.
 *
 * @property bips, default is 10 bips
 * @property feePercentage fee as a percentage of total execution cost, 0.01 = 1%. Default is 0.0
 * @constructor Create new Default cost model
 */
class DefaultCostModel(
    private val bips: Double = 10.0,
    private val feePercentage: Double = 0.0,
    private val priceType: String = "DEFAULT"
) : CostModel {

    override fun calculatePrice(order: Order, priceAction: PriceAction): Double {
        val price = priceAction.getPrice(priceType)
        val correction = if (order.remaining > 0) 1.0 + bips / 10_000.0 else 1.0 - bips / 10_000.0
        return price * correction
    }

    override fun calculateFee(execution: Execution): Double {
        return execution.size().absoluteValue * execution.price * feePercentage
    }

}

/**
 * Cost model that adds no additional spread, slippage or other fees to the transaction cost. Mostly useful to see how
 * a strategy would perform without additional cost. But not very realistic and should be avoided in realistic
 * back tests scenarios since it doesn't reflect live trading.
 */
class NoCostModel(private val priceType: String = "DEFAULT") : CostModel {

    override fun calculatePrice(order: Order, priceAction: PriceAction) = priceAction.getPrice(priceType)

    override fun calculateFee(execution: Execution): Double = 0.0

}
