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

/**
 * `One Cancels Other` order. If either the first or second order is executed, the other one will be cancelled. This
 * implementation requires that both orders have the same size and asset.
 *
 * @property first the first of the two OCO orders
 * @property second the second of the two OCO orders
 * @param tag an optional tag
 *
 * @constructor create a new instance of an OCOOrder
 */
class OCOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    tag: String = ""
) : Order(first.asset, tag) {

    init {
        require(first.asset == second.asset) { "OCO orders can only contain orders for the same asset" }
        require(first.size == second.size) { "OCO orders need to have the same size" }
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)
}

