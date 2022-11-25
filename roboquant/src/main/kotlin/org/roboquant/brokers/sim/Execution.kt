/*
 * Copyright 2020-2022 Neural Layer
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

import org.roboquant.common.Size
import org.roboquant.orders.CreateOrder

/**
 * Return how much of the order was filled and against what price. The price is in
 * the currency denoted by the underlying asset.
 *
 * @property order The order the execution is linked to
 * @property size The quantity traded
 * @property price The (average) price of the execution
 * @constructor Create new Execution
 */
class Execution(val order: CreateOrder, val size: Size, val price: Double) {

    init {
        require(size.nonzero) { "Execution should have a non-zero quantity" }
    }

    /**
     * Total value of execution in the currency denoted by the underlying asset
     */
    val value
        get() = order.asset.value(size, price).value

    /**
     * Total amount of execution in the currency denoted by the underlying asset
     */
    val amount
        get() = order.asset.value(size, price)
}
