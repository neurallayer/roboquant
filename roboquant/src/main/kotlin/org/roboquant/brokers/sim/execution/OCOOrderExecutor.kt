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

import org.roboquant.brokers.sim.Pricing
import org.roboquant.orders.CreateOrder
import org.roboquant.orders.OCOOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant

internal class OCOOrderExecutor(override val order: OCOOrder) : CreateOrderExecutor<OCOOrder> {

    private val first = ExecutionEngine.getCreateOrderExecutor(order.first)
    private val second = ExecutionEngine.getCreateOrderExecutor(order.second)
    private var active = 0

    override var status: OrderStatus = OrderStatus.INITIAL

    /**
     * Cancel the order, return true if successful, false otherwise
     */
    /**
     * Cancel the order, return true if successful, false otherwise
     */
    override fun cancel(time: Instant): Boolean {
        return if (status.closed) {
            false
        } else {
            first.cancel(time)
            second.cancel(time)
            status = OrderStatus.CANCELLED
            true
        }
    }

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        status = OrderStatus.ACCEPTED

        if (active == 0 || active == 1) {
            val result = first.execute(pricing, time)
            if (result.isNotEmpty()) {
                active = 1
                status = first.status
                return result
            }

        }

        if (active == 0 || active == 2) {
            val result = second.execute(pricing, time)
            if (result.isNotEmpty()) {
                active = 2
                status = second.status
                return result
            }
        }

        return emptyList()
    }

    override fun update(order: CreateOrder, time: Instant) = false
}