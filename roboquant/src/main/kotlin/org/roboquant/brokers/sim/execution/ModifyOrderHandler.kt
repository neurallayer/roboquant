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

package org.roboquant.brokers.sim.execution

import org.roboquant.orders.OrderState
import org.roboquant.orders.*
import java.time.Instant


private fun List<CreateOrderHandler>.getHandler(orderId: Int) : CreateOrderHandler? =
    firstOrNull {
        it.state.order.id == orderId
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
    override fun execute(handlers: List<CreateOrderHandler>, time: Instant) {
        val handler = handlers.getHandler(order.original.orderId)

        state = if (handler != null && handler.update(order.update, time)) {
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
    override fun execute(handlers: List<CreateOrderHandler>, time: Instant) {
        val handler = handlers.getHandler(order.state.orderId )
        state = if (handler !== null && handler.cancel(time)) {
            OrderState(order, OrderStatus.COMPLETED, time, time)
        } else {
            OrderState(order, OrderStatus.REJECTED, time, time)
        }
    }
}

/**
 * Simulate the execution of a CancelAllOrder and cancel all open orders
 */
internal class CancelAllOrderHandler(val order: CancelAllOrder) : ModifyOrderHandler {

    override var state: OrderState = OrderState(order)

    /**
     * Cancel the orders for the provided [handlers] and [time]
     */
    override fun execute(handlers: List<CreateOrderHandler>, time: Instant) {
        for (handler in handlers) handler.cancel(time)
        state = OrderState(order, OrderStatus.COMPLETED, time, time)
    }
}