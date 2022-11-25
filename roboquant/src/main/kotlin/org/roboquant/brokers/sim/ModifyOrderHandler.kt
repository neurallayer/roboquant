/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers.sim

import org.roboquant.orders.OrderState
import org.roboquant.orders.*
import java.time.Instant

private fun List<OrderHandler>.getSingleOrderHandler(id: Int) =
    filterIsInstance<SingleOrderHandler<SingleOrder>>().firstOrNull {
        it.order.id == id
    }


/**
 * Simulate the execution of a UpdateOrder. Currently, no additional checks in place if the
 * actual update would be valid in real life trading.
 */
internal class UpdateOrderHandler(val order: UpdateOrder) : ModifyOrderHandler {

    override var state: OrderState = OrderState(order)

    /**
     * Update the orders for the provided [handlers] and [time]
     */
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


/**
 * Simulate the execution of a CancelOrder
 */
internal class CancelOrderHandler(val order: CancelOrder) : ModifyOrderHandler {

    override var state: OrderState = OrderState(order)

    /**
     * Cancel the orders for the provided [handlers] and [time]
     */
    override fun execute(handlers: List<OrderHandler>, time: Instant) {
        val handler = handlers.getSingleOrderHandler(order.state.id)
        state = if (handler != null && handler.state.status.open) {
            handler.state = handler.state.copy(time, OrderStatus.CANCELLED)
            OrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            OrderState(order, OrderStatus.REJECTED, time, time)
        }
    }
}
