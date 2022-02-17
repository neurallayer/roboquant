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

package org.roboquant.orders


/**
 * Create a cancellation order for another open [Order]. It is not guaranteed that a cancellation is
 * also successfully executed, since it can be the case that order is already filled before the cancellation
 * is processed.
 *
 * @property order The order that needs to be cancelled
 * @constructor
 */
class CancellationOrder(val order: Order) : Order(order.asset) {

    init {
        require(order.status.open) { "Only open orders can be cancelled" }
    }

    override fun clone(): CancellationOrder {
        val result = CancellationOrder(order.clone())
        copyTo(result)
        return result
    }



}
