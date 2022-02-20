package org.roboquant.brokers.sim

import org.roboquant.orders.Order
import org.roboquant.orders.OrderState
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

}



