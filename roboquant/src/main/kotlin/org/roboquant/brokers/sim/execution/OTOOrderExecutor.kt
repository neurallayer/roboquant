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

import org.roboquant.brokers.sim.Pricing
import org.roboquant.orders.CancelOrder
import org.roboquant.orders.ModifyOrder
import org.roboquant.orders.OTOOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant

internal class OTOOrderExecutor(override val order: OTOOrder) : OrderExecutor<OTOOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL

    private val first = ExecutionEngine.getExecutor(order.primary)
    private val second = ExecutionEngine.getExecutor(order.secondary)

    /**
     * Cancel the order, return true if successful, false otherwise
     */
    fun cancel(cancelOrder: CancelOrder, time: Instant): Boolean {
        return if (status.closed) {
            false
        } else {
            first.modify(cancelOrder, time)
            second.modify(cancelOrder, time)
            status = OrderStatus.CANCELLED
            true
        }
    }

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        status = OrderStatus.ACCEPTED
        val result = mutableListOf<Execution>()

        if (first.status.open) {
            result.addAll(first.execute(pricing, time))
            if (first.status.aborted) status = first.status
        }

        if (first.status == OrderStatus.COMPLETED) {
            result.addAll(second.execute(pricing, time))
            status = second.status
        }

        return result
    }

    override fun modify(modifyOrder: ModifyOrder, time: Instant): Boolean {
        return when(modifyOrder) {
            is CancelOrder -> cancel(modifyOrder, time)
            else -> false
        }
    }


}