package org.roboquant.brokers.sim

import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant

/**
 * Order handler
 *
 * @param T
 * @property order
 * @constructor Create empty Order handler
 */
abstract class OrderHandler<T: Order>(var order: T) {

    abstract fun execute(pricing: Pricing, time: Instant): List<Execution>

    val state: OrderState
        get() = OrderState(order, status, open, closed)

    var status: OrderStatus = OrderStatus.INITIAL
    var open: Instant = Instant.MIN
    var closed: Instant = Instant.MAX

    fun update(time: Instant) {
        if (state.status === OrderStatus.INITIAL) {
            status = OrderStatus.ACCEPTED
            open = time
        }
    }

    fun close(status: OrderStatus, time: Instant) {
        this.status = status
        closed = time
        if (open == Instant.MIN) open = time
    }


}



