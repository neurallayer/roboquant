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

package org.roboquant.orders

/**
 * One Cancels Other order
 *
 * @property first
 * @property second
 *
 * @constructor
 * @param id
 * @param tag
 */
class OCOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(first.asset, id, tag) {

    init {
        require(first.asset == second.asset) { "OCO orders can only contain orders for the same asset" }
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)
}

/**
 * One Triggers Other order
 *
 * @property first
 * @property second
 * @constructor
 *
 * @param id
 * @param tag
 */
class OTOOrder(
    val first: SingleOrder,
    val second: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(first.asset, id, tag) {

    init {
        require(first.asset == second.asset) { "OTO orders can only contain orders for the same asset" }
    }

    override fun info() = sortedMapOf("first" to first, "second" to second)

}

/**
 * Bracket order
 *
 * @property entry
 * @property takeProfit
 * @property stopLoss
 * @constructor
 *
 * @param id
 * @param tag
 */
class BracketOrder(
    val entry: SingleOrder,
    val takeProfit: SingleOrder,
    val stopLoss: SingleOrder,
    id: Int = nextId(),
    tag: String = ""
) : Order(entry.asset, id, tag) {

    init {
        require(entry.asset == takeProfit.asset && entry.asset == stopLoss.asset) {
            "Bracket orders can only contain orders for the same asset"
        }
        require(entry.size == -takeProfit.size && entry.size == -stopLoss.size) {
            "Bracket orders takeProfit and stopLoss orders need to close position"
        }
    }

    override fun info() = sortedMapOf("entry" to entry, "takeProfit" to takeProfit, "stopLoss" to "stopLoss")
}

