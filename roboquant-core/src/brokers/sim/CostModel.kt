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

import org.roboquant.common.Cash
import org.roboquant.feeds.PriceAction
import org.roboquant.orders.Order
import java.lang.Double.max
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * Calculate the cost of an execution
 *
 */
interface CostModel {

    fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double>
}

/**
 * Default cost model, using a fixed percentage  expressed in basis points and no additional commission fee. This
 * percentage would cover both spread and slippage. There is no fixed fee.
 *
 * @property bips, default is 10 bips
 * @constructor Create new Default cost model
 */
class DefaultCostModel(private val bips: Double = 10.0) : CostModel {

    override fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double> {
        val fee = 0.0
        val correction = if (execution.quantity > 0) 1.0 + bips / 10_000.0 else 1.0 - bips / 10_000.0
        val cost = execution.price * correction * execution.size()
        return Pair(cost, fee)
    }

}


/**
 * Cost model, using a fixed percentage slippage expressed in basis points and additional commission fee. The commission
 * fee is also in expressed in bips but has a minimum and maximum amount (for each currency used).
 *
 *
 * @property slippage, slippage in bips
 * @property fee, fee in bips
 *
 * @constructor Create empty Default cost model
 */
class CommissionBasedCostModel(
    private val slippage: Int = 5,
    private val fee: Int = 10,
    private val minimumAmount: Cash = Cash(),
    private val maximumAmount: Cash = Cash()
) : CostModel {

    override fun calculate(order: Order, execution: Execution, price: PriceAction): Pair<Double, Double> {
        val correction = if (execution.quantity > 0) 1.0 + slippage / 10_000.0 else 1.0 - slippage / 10_000.0
        val cost = execution.price * correction * execution.size()

        val currency = order.asset.currency
        var fee = cost.absoluteValue * (fee/10_000.0)
        fee = max(fee, minimumAmount.getAmount(currency))
        val maxAmount = maximumAmount.getAmount(currency)
        if (maxAmount != 0.0) fee = min(fee, maxAmount)
        return Pair(cost, fee)
    }

}