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

import org.roboquant.common.Asset


/**
 * Order is an instruction for a broker to initiate a certain action. An order is always associated with a
 * single [asset].
 *
 * Within roboquant it is the [policy][org.roboquant.policies.Policy] that creates the orders. An order can cover a
 * wide variety of use cases:
 *
 * - a new order (perhaps the most common use case), ranging from a simple market order to advanced order types
 * - cancellation of an existing order
 * - update of an existing order
 **/
abstract class Order(val asset: Asset, val id: String) {

    init {
        require(id.isNotBlank()) { "Cannot use blank order id's"}
    }

    companion object {

        // Counter used for creating unique order ids
        internal var ID = 0L

        /**
         * Generate the next order id
         */
        fun nextId(): String {
            synchronized(ID) {
                return ID++.toString()
            }
        }
    }

    override fun toString(): String {
        return "$name id=$id asset=$asset ${info()}"
    }

    open val name
        get() = this::class.simpleName

    /**
     * Provide extra info as map
     *
     * @return
     */
    abstract fun info() : Map<String, Any>

}


val Collection<Order>.initialOrderSlips
    get() = map { OrderState(it, OrderStatus.INITIAL)}



