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

import org.roboquant.common.Asset

/**
 * An order is an instruction for a broker to buy or sell an asset or modify an existing order. An order in *roboquant*
 * is always associated with a single [asset] and it is a [policy][org.roboquant.policies.Policy] that creates orders.
 *
 * An order can cover different of use cases:
 *
 * - a buy or sell order (perhaps the most common use case), ranging from simple market order to advanced order types
 * - cancellation of an existing order
 * - update of an existing order
 *
 * @property asset the underlying asset of the order
 * @property id a unique id of the order
 * @property tag an arbitrary tag that can be associated with this order, default is an empty string
 *
 **/
abstract class Order(val asset: Asset, val id: Int, val tag: String = "") {

    companion object {

        // Counter used for creating unique order ids
        var ID = 0

        /**
         * Generate the next order id
         */
        @Synchronized
        fun nextId(): Int {
            return ID++
        }
    }

    override fun toString(): String {
        return "$type id=$id asset=${asset.symbol} tag=$tag ${info()}"
    }

    /**
     * What is the type of order, default is the class name without the order suffix
     */
    open val type
        get() = this::class.simpleName?.removeSuffix("Order")?.uppercase() ?: "UNKNOWN"

    /**
     * Provide extra info as map, used in displaying order information. Default is an empty map.
     *
     * @return
     */
    open fun info(): Map<String, Any> = emptyMap()

}



