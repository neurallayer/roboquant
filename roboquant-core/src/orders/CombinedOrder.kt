package org.roboquant.orders

import java.time.Instant

/**
 * Base class for combined orders.
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

}