package org.roboquant.brokers.sim.execution

import org.roboquant.orders.OrderStatus
import org.roboquant.orders.UpdateOrder
import java.time.Instant

/**
 * Simulate the execution of a UpdateOrder. Currently, no additional checks in place if the
 * actual update would be valid in real life trading.
 */
internal class UpdateOrderExecutor(override val order: UpdateOrder) : ModifyOrderExecutor<UpdateOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL

    /**
     * Update the orders for the provided [handlers] and [time]
     */
    override fun execute(handlers: List<CreateOrderExecutor<*>>, time: Instant) {
        status = OrderStatus.ACCEPTED
        val handler = handlers.getExecutor(order.original.orderId)

        status = if (handler != null && handler.update(order.update, time)) {
            OrderStatus.COMPLETED
        } else {
            OrderStatus.REJECTED
        }

    }
}