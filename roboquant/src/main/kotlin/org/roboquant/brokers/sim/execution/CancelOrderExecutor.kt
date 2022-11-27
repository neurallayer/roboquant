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

import org.roboquant.orders.CancelOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant


internal fun List<CreateOrderExecutor<*>>.getExecutor(orderId: Int) : CreateOrderExecutor<*>? =
    firstOrNull {
        it.order.id == orderId
    }


/**
 * Simulate the execution of a CancelOrder
 */
internal class CancelOrderExecutor(override val order: CancelOrder) : ModifyOrderExecutor<CancelOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL

    /**
     * Cancel the orders for the provided [handlers] and [time]
     */
    override fun execute(handlers: List<CreateOrderExecutor<*>>, time: Instant) {
        status = OrderStatus.ACCEPTED
        val handler = handlers.getExecutor(order.state.orderId )
        status = if (handler !== null && handler.cancel(time)) {
            OrderStatus.COMPLETED
        } else {
            OrderStatus.REJECTED
        }
    }
}
