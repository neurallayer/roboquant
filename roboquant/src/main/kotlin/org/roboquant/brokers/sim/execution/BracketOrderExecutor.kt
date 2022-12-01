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
import org.roboquant.orders.BracketOrder
import org.roboquant.orders.CreateOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant

internal class BracketOrderExecutor(override val order: BracketOrder) : CreateOrderExecutor<BracketOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL
    private val entry = ExecutionEngine.getExecutor(order.entry) as SingleOrderExecutor<*>
    private val profit = ExecutionEngine.getExecutor(order.takeProfit) as SingleOrderExecutor<*>
    private val loss = ExecutionEngine.getExecutor(order.stopLoss) as SingleOrderExecutor<*>

    /**
     * Cancel the order, return true if successful, false otherwise
     */
    override fun cancel(time: Instant) : Boolean {
        return if (status.closed) {
            false
        } else {
            entry.cancel(time)
            profit.cancel(time)
            loss.cancel(time)
            status = OrderStatus.CANCELLED
            true
        }
    }

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        status = OrderStatus.ACCEPTED
        if (entry.status.open) return entry.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        val remaining = entry.order.size + loss.fill + profit.fill
        if (remaining.iszero) status = OrderStatus.COMPLETED
        return executions
    }

    override fun update(order: CreateOrder, time: Instant) = false

}
