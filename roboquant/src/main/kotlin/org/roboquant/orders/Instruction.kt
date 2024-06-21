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

import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import java.time.Instant

/**
 * An instruction for a broker. An sintruction can cover different of use cases:
 *
 * - A buy- or sell-order (the most common use case), ranging from simple market order to advanced order types
 * - Cancellation of an existing order
 * - Update of an existing order
 **/
sealed class Instruction


/**
 * Base class for all types of create orders. This ranges from a simple [MarketOrder], all the way to advanced order
 * types like a [BracketOrder].
 */
abstract class Order(val asset: Asset, val tag: String) : Instruction() {

    /**
     * The order id is set by broker once placed, before that it is an empty string.
     * The exception are modify and cancel orders that have the id of the underlying order.
     */
    var id = ""


    /**
     * Status of the order, set to INITIAL when just created
     */
    var status = OrderStatus.INITIAL

    /**
     * Returns true the order status is open, false otherwise
     */
    val open: Boolean
        get() = status.open

    /**
     * Returns true the order status is closed, false otherwise
     */
    val closed: Boolean
        get() = status.closed

    var openedAt: Instant = Timeframe.MIN

    fun cancel(): Cancellation {
        return Cancellation(id)
    }

    fun modify(updateOrder: Order) : Modification {
        return Modification(id, updateOrder)
    }

    /**
     * What is the type of instruction, default is the class name without any order suffix
     */
    open val type: String
        get() = this::class.simpleName?.uppercase()?.removeSuffix("ORDER") ?: "UNKNOWN"

    /**
     * Provide extra info as a map, used in displaying order information. Default is an empty map and subclasses are
     * expected to return a map with their additional properties like limit or trailing percentages.
     */
    open fun info(): Map<String, Any> = emptyMap()


    /**
     * Returns a unified string representation for the different order types
     */
    override fun toString(): String {
        val infoStr = info().toString().removePrefix("{").removeSuffix("}")
        return "type=$type id=$id tag=$tag $infoStr"
    }

}





