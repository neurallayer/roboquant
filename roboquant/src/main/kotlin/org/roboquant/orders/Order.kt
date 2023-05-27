/*
 * Copyright 2020-2023 Neural Layer
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
 * An order is an instruction for a broker to buy or sell an asset or modify an existing order.
 *
 * An order can cover different of use cases:
 *
 * - a buy or sell order (the most common use case), ranging from simple market order to advanced order types
 * - cancellation of an existing order
 * - update of an existing order
 *
 * An order doesn't necessary has a size. For example, in case of a cancellation order. But every order is linked to
 * a single asset.
 *
 * @property asset the underlying asset of the order
 * @property id a unique order identifier
 * @property tag an (optional) tag that can be used to store additional information
 **/
sealed class Order(val asset: Asset, val tag: String) {

    /**
     * The order identifier that is automatically generated and unique per process
     */
    val id = nextId()

    /**
     * @suppress
     */
    companion object {

        /**
         * Counter used for creating unique order ids.
         */
        private var ID = 0

        /**
         * Set the order id to its [initial value][initialValue]
         */
        @Synchronized
        fun setId(initialValue: Int) {
            ID = initialValue
        }

        /**
        * Generate the next order id
         */
        @Synchronized
        private fun nextId(): Int {
            return ID++
        }
    }


    /**
     * What is the type of order, default is the class name without any order suffix
     */
    open val type: String
        get() = this::class.simpleName?.uppercase()?.removeSuffix("ORDER") ?: "UNKNOWN"

    /**
     * Provide extra info as map, used in displaying order information. Default is an empty map and subclasses are
     * expected to return a map with their additional properties like limit or trailing percentages.
     */
    open fun info(): Map<String, Any> = emptyMap()

    /**
     * Returns a unified string representation for the different order types
     */
    override fun toString(): String {
        val infoStr = info().toString().removePrefix("{").removeSuffix("}")
        return "type=$type id=$id asset=${asset.symbol} tag=$tag $infoStr"
    }

}

/**
 * Base class for all types of create orders. This ranges from a simple [MarketOrder], all the way to advanced order
 * types like a [BracketOrder].
 */
abstract class CreateOrder(asset: Asset, tag: String) : Order(asset, tag)

/**
 * Base class for all types of modify-orders. Two most commonly used subclasses are the [CancelOrder] and [UpdateOrder].
 *
 * Please note that modify orders by design can only modify createOrders
 *
 * @property order the (create-)order that will be modified
 * @param tag an optional tag
 */
abstract class ModifyOrder(val order: CreateOrder, tag: String) : Order(order.asset, tag)




