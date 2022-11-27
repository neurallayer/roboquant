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
 * An order is an instruction for a broker to buy or sell an asset or modify an existing order.
 *
 * An order can cover different of use cases:
 *
 * - a buy or sell order (perhaps the most common use case), ranging from simple market order to advanced order types
 * - cancellation of an existing order
 * - update of an existing order
 *
 * An order doesn't necessary have a size, for example in case of a cancellation order. But every order is linked to
 * a single asset.
 *
 * @property tag an arbitrary tag that can be associated with this order, default is an empty string
 * @property asset the underlying asset of the order
 *
 **/
sealed class Order(val asset: Asset, val tag: String) {

    /**
     * order identifier that is automatically generated and unique per process
     */
    val id = nextId()

    /**
     * @suppress
     */
    companion object {

        /**
         * Counter used for creating unique order ids. Normally you should not manually change this value since the
         * unique ID generation is a manual process.
         */
        var ID = 0

        /**
         * Generate the next order id
         */
        @Synchronized
        private fun nextId(): Int {
            return ID++
        }
    }


    /**
     * What is the type of order, default is the class name without the order suffix
     */
    open val type
        get() = this::class.simpleName?.removeSuffix("Order")?.uppercase() ?: "UNKNOWN"

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
 * Base class for all types of create orders.
 */
abstract class CreateOrder(asset: Asset, tag: String) : Order(asset, tag)

/**
 * Base class for all types of modify orders. Two most commonly used sub-classed are the CancelOrder and UpdateOrder.
 *
 * lease note that Modify orders can only modify createOrders
 */
abstract class ModifyOrder(asset: Asset, tag: String) : Order(asset, tag)




