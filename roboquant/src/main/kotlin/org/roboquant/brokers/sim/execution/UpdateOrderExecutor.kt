package org.roboquant.brokers.sim.execution

import org.roboquant.orders.CreateOrder
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.UpdateOrder
import java.time.Instant

/**
 * Simulate the execution of a UpdateOrder. Currently, no additional checks are in place if the actual update would
 * be valid in real life trading.
 */
internal class UpdateOrderExecutor(override val order: UpdateOrder) : ModifyOrderExecutor<UpdateOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL

    override val createOrder: CreateOrder
        get() = order.order

    /**
     * Update the orders for the provided [executor] and [time]
     */
    override fun execute(executor: CreateOrderExecutor<*>?, time: Instant) {
        if (executor == null) {
            status = OrderStatus.REJECTED
            return
        }

        status = OrderStatus.ACCEPTED
        status = if (executor.update(order.update, time)) {
            OrderStatus.COMPLETED
        } else {
            OrderStatus.REJECTED
        }

    }
}