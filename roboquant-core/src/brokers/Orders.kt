package org.roboquant.brokers

import org.roboquant.common.Summary
import org.roboquant.orders.Order
import org.roboquant.orders.OrderStatus
import java.util.*

/**
 * Optimized container for storing orders used by the Account.
 *
 * @see Order
 *
 * @constructor Create new Orders container
 */
class Orders : MutableList<Order>, LinkedList<Order>()  {

    /**
     * Closed orders, any order that is in a state that cannot be further processed
     */
    val closed
        get() = filter { it.status.closed }

    /**
     * Open orders, these are orders that can still be processed
     */
    val open
        get() = filter { it.status.open }


    /**
     * Orders that are in [OrderStatus.ACCEPTED] state, and so they are ready for execution
     */
    val accepted
        get() = filter { it.status === OrderStatus.ACCEPTED }

    /**
     * Provide a summary for the orders, split by open and closed orders
     *
     * @return
     */
    fun summary() : Summary {
        val s = Summary("Orders")

        val c = Summary("closed")
        for (order in closed) c.add("$order")
        if (closed.isEmpty()) c.add("Empty")
        s.add(c)

        val o = Summary("open")
        for (order in open) o.add("$order")
        if (open.isEmpty()) o.add("Empty")
        s.add(o)

        return s
    }



    /**
     *
     * @param orders
     */
    internal fun put(orders: Orders) {
        addAll(orders.map { it.clone() })
    }

}
