package org.roboquant.brokers.sim

import org.roboquant.orders.*
import java.time.Instant

internal class UpdateOrderHandler(order: UpdateOrder, private val handlers: List<OrderHandler<*>>) : OrderHandler<UpdateOrder>(order) {

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        val handler = handlers.filterIsInstance<OrderHandler<SingleOrder>>().firstOrNull { it.order.id == order.original.id }
        if (handler != null && handler.status.open) {
           handler.order = order.update
            close(OrderStatus.COMPLETED, time)
        } else {
            close(OrderStatus.REJECTED, time)
        }

        return emptyList()
    }
}


internal class CancelOrderHandler(order: CancelOrder, private val handlers: List<OrderHandler<*>>) : OrderHandler<CancelOrder>(order) {

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        val handler = handlers.firstOrNull { it.order.id == order.order.id }
        if (handler != null && handler.status.open) {
            handler.close(OrderStatus.EXPIRED, time)
            close(OrderStatus.COMPLETED, time)
        } else {
            close(OrderStatus.REJECTED, time)
        }
        return emptyList()
    }
}
