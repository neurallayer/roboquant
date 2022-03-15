package org.roboquant.brokers.sim

import org.roboquant.brokers.DefaultOrderState
import org.roboquant.brokers.update
import org.roboquant.orders.*
import java.time.Instant

private fun List<OrderHandler>.getSingleOrderHandler(id: Int) =
    filterIsInstance<SingleOrderHandler<SingleOrder>>().firstOrNull {
        it.order.id == id
    }

internal class UpdateOrderHandler(val order: UpdateOrder) : ModifyOrderHandler {

    override var state: OrderState = DefaultOrderState(order)

    override fun execute(handlers: List<OrderHandler>, time: Instant) {
        val handler = handlers.getSingleOrderHandler(order.original.id)
        state = if (handler != null && handler.state.status.open) {
            handler.order = order.update
            DefaultOrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            DefaultOrderState(order, OrderStatus.REJECTED, time, time)
        }

    }
}

internal class CancelOrderHandler(val order: CancelOrder) : ModifyOrderHandler {

    override var state: OrderState = DefaultOrderState(order)

    override fun execute(handlers: List<OrderHandler>, time: Instant) {
        val handler = handlers.getSingleOrderHandler(order.order.id)
        state = if (handler != null && handler.state.status.open) {
            handler.state = handler.state.update(time, OrderStatus.EXPIRED)
            DefaultOrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            DefaultOrderState(order, OrderStatus.REJECTED, time, time)
        }
    }
}
