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

import org.roboquant.common.nonzero
import org.roboquant.orders.Order

/**
 * Return how much of the order was filled and against what price. The price is in
 * the currency denoted by the underlying asset.
 *
 * @property order The order the execution is linked to
 * @property quantity The quantity traded
 * @property price The (average) price of the execution
 * @constructor Create new Execution
 */
class Execution(val order: Order, val quantity: Double, val price: Double) {

    init {
        require(quantity.nonzero) { "Execution should have a non-zero quantity" }
    }

    /**
     * Totol size of execution, including contract size
     */
    val size
        get() = order.asset.multiplier * quantity

    /**
     * Totol value of execution in the currency denoted by the underlying asset
     */
    val value
        get() = order.asset.value(quantity, price).value

    /**
     * Totol amount of execution in the currency denoted by the underlying asset
     */
    val amount
        get() = order.asset.value(quantity, price)
}
