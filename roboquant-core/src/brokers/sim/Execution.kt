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

import org.roboquant.common.Amount
import org.roboquant.orders.Order

/**
 * Return how much of the order was filled and against what price. The price is in
 * the currency denoted by the underlying asset.
 *
 * Typically a [CostModel] might still increase the price afterwards due to spread and slippage cost.
 *
 * @property order
 * @property quantity
 * @property price
 * @constructor Create empty Execution
 */
class Execution(val order: Order, val quantity: Double, val price: Double) {

    init {
        require(quantity != 0.0) { "Execution should have a non-zero quantity" }
    }

    /**
     * Totol size of execution, including contract size
     */
    fun size(): Double = order.asset.multiplier * quantity

    fun value() : Amount = order.asset.value(quantity, price)
}
