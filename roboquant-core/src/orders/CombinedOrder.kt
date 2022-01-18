package org.roboquant.orders

import java.time.Instant
import kotlin.math.absoluteValue

/**
 * Base class for combined orders that takes care of setting some of the properties
 */
abstract class CombinedOrder(vararg val children: Order) : Order(children.first().asset) {

    init {
        require(children.distinctBy { it.asset }.size == 1) { "All orders require to have the same asset"}
    }

    override var placed: Instant = Instant.MIN
        set(value) {
            field = value
            for (order in children) order.placed = value
        }

    override var price: Double = Double.NaN
        set(value) {
            field = value
            for (order in children) order.price = value
        }

    override fun getValue(price: Double): Double = children.maxOf { it.getValue(price).absoluteValue }

}