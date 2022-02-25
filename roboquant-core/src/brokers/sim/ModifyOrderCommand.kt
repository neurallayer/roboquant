package org.roboquant.brokers.sim

import org.roboquant.orders.*
import java.time.Instant

internal class UpdateOrderHandler(order: UpdateOrder, cmds: List<OrderHandler<*>>) : OrderHandler<UpdateOrder>(order) {

    private val ro = cmds.filterIsInstance<OrderHandler<Order>>()

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        val idx = ro.indexOfFirst { it.order.id == order.original.id }
        if (idx >=0 ) {
            ro[idx].order = order.update
            close(OrderStatus.COMPLETED, time)
        } else {
            close(OrderStatus.REJECTED, time)
        }

        return emptyList()
    }
}


internal class CancelOrderHandler(order: CancelOrder, private val cmds: List<OrderHandler<*>>) : OrderHandler<CancelOrder>(order) {

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        val cmd = cmds.firstOrNull { it.order.id == order.order.id }
        if (cmd == null) {
            close(OrderStatus.REJECTED, time)
        } else {
            if (cmd.status.closed) {
                close(OrderStatus.REJECTED, time)
            } else {
                cmd.close(OrderStatus.EXPIRED, time)
                close(OrderStatus.COMPLETED, time)
            }

        }
        return emptyList()
    }
}
