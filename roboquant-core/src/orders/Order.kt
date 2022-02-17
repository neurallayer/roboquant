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
import java.time.Instant

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
 *
 * Please note it depends on the broker implementation which order types are supported.
 *
 */
abstract class Order(val asset: Asset) : Cloneable {

    /**
     * OrderId, initially set when creating an order. This id will remain unique until the JVM stops.
     */
    var id = getId().toString()
        private set


    /**
     * What is the latest status of this order, see also [OrderStatus] for the possible values
     */
    var status = OrderStatus.INITIAL

    /**
     * When was the order first placed. This timestamp is used during simulation to deal with time-in-force policies.
     * Before the order is actually placed, this attribute will contain the value `Instant.MIN`
     */
    open var placed: Instant = Instant.MIN

    companion object {

        // Counter for unique order id
        internal var ID = 0L

        private fun getId(): Long {
            synchronized(ID) {
                return ID++
            }
        }

    }

    public override fun clone(): Order {
        throw NotImplementedError("Concrete subclasses of Order need to override clone() method")
    }


    /**
     * Copy current state into the passed object. This is used in the clone function of the subclasses
     */
    protected fun copyTo(result: Order) {
        result.id = id
        result.placed = placed
        result.status = status
    }



}

