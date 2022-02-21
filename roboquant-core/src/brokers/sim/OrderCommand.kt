package org.roboquant.brokers.sim

import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant


abstract class OrderCommand<T: Order>(var order: T) {

    abstract fun execute(pricing: Pricing, time: Instant): List<Execution>

    var state = OrderState()

    /**
     * Make sure we use copy
     */
    var status
        get() = state.status
        set(value) {
            state = state.copy(status = value)
        }

    fun update(time: Instant) {
        if (state.status === OrderStatus.INITIAL) {
            state = OrderState(OrderStatus.ACCEPTED, time)
        }
    }

    fun close(status: OrderStatus, time: Instant) {
        if (state.status === OrderStatus.ACCEPTED) {
            state = OrderState(status, state.placed, time)
        }
    }


}



