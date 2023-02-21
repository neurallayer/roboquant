/*
 * Copyright 2020-2023 Neural Layer
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