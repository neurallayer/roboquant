package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import java.time.Instant


/**
 * Create a cancellation order for another open [Order]. It is not guaranteed that a cancellation is
 * also successfully executed, since it can be the case that order is already filled before the cancellation
 * is processed.
 *
 * @property order The order that needs to be cancelled
 * @constructor
 *
 * @param tag
 */
class CancellationOrder(val order: Order, var tag: String = "") : Order(order.asset) {

    override fun clone(): CancellationOrder {
        val result = CancellationOrder(order.clone(), tag)
        copyTo(result)
        return result
    }

    override fun execute(price: Double, time: Instant) : List<Execution> {
        place(price, time)

        if (! order.status.closed) {
            order.status = OrderStatus.CANCELLED
            status = OrderStatus.COMPLETED
        } else {
            status = OrderStatus.REJECTED
        }
        return listOf()
    }

}
