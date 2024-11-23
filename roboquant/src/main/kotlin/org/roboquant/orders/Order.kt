package org.roboquant.orders

import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import java.time.Instant

/**
 * Base class for all types of create orders. This ranges from a simple [MarketOrder], all the way to advanced order
 * types like a [BracketOrder].
 *
 * The only thing they all have in common is they refer to a single [asset] and can optionally have a tag associated with them.
 */
abstract class Order(val asset: Asset, val tag: String="") : Instruction() {

    /**
     * The order id is set by broker once placed, before that it contains an empty string.
     */
    var id = ""

    /**
     * Status of the order, set to CREATED when just created
     */
    var status = OrderStatus.CREATED

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
