package org.roboquant.brokers.sim

import org.roboquant.orders.*
import java.time.Instant


private fun List<OrderHandler>.getSingleOrderHandler(id: String) = filterIsInstance<SingleOrderHandler<SingleOrder>>().firstOrNull {
    it.order.id == id
}


internal class UpdateOrderHandler(val order: UpdateOrder) : ModifyOrderHandler {

    override var state: OrderState = OrderState(order)

    override fun execute(handlers: List<OrderHandler>, time: Instant) {
        val handler = handlers.getSingleOrderHandler(order.original.id)
        state = if (handler != null && handler.state.status.open) {
            handler.order = order.update
            OrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            OrderState(order, OrderStatus.REJECTED, time, time)
        }

    }
}


internal class CancelOrderHandler(val order: CancelOrder) : ModifyOrderHandler {

    override var state: OrderState = OrderState(order)

    override fun execute(handlers: List<OrderHandler>, time: Instant) {
        val handler = handlers.getSingleOrderHandler(order.order.id)
        state = if (handler != null && handler.state.status.open) {
            handler.state = handler.state.update(time, OrderStatus.EXPIRED)
            OrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            OrderState(order, OrderStatus.REJECTED, time, time)
        }
    }
}
