package org.roboquant.brokers.sim

import org.roboquant.orders.*
import java.time.Instant

internal class UpdateOrderCommand(order: UpdateOrder, cmds: List<OrderCommand<*>>) : OrderCommand<UpdateOrder>(order) {

    private val ro = cmds.filterIsInstance<OrderCommand<Order>>()

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


internal class CancelOrderCommand(order: CancellationOrder, private val cmds: List<OrderCommand<*>>) : OrderCommand<CancellationOrder>(order) {

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        val idx = cmds.indexOfFirst { it.order.id == order.id }
        if (idx >=0 ) {
            cmds[idx].close(OrderStatus.CANCELLED, time)
            close(OrderStatus.COMPLETED, time)
        } else {
            close(OrderStatus.REJECTED, time)
        }

        return emptyList()
    }
}
