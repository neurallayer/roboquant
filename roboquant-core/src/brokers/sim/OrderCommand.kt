package org.roboquant.brokers.sim

import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant


abstract class OrderCommand<T: Order>(var order: T) {

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
        if (state.status === OrderStatus.ACCEPTED) {
            this.status = status
            closed = time
        }
    }


}



