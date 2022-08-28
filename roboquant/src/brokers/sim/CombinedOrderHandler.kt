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

import org.roboquant.orders.*
import java.time.Instant

internal class OCOOrderHandler(val order: OCOOrder) : TradeOrderHandler{

    private val first = ExecutionEngine.getHandler(order.first) as TradeOrderHandler
    private val second = ExecutionEngine.getHandler(order.second) as TradeOrderHandler
    private var active = 0

    override var state : OrderState = OrderState(order)


    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        state = state.copy(time)

        if (active == 0 || active == 1) {
            val result = first.execute(pricing, time)
            if (result.isNotEmpty()) {
                active = 1
                state = state.copy(time, first.state.status)
                return result
            }

        }

        if (active == 0 || active == 2) {
            val result = second.execute(pricing, time)
            if (result.isNotEmpty()) {
                active = 2
                state = state.copy(time, second.state.status)
                return result
            }
        }

        return emptyList()
    }
}


internal class OTOOrderHandler(val order: OTOOrder) : TradeOrderHandler {

    override var state : OrderState = OrderState(order)

    private val first = ExecutionEngine.getHandler(order.first) as TradeOrderHandler
    private val second = ExecutionEngine.getHandler(order.second) as TradeOrderHandler

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        state = state.copy(time)
        val result = mutableListOf<Execution>()

        if (first.state.status.open) {
            result.addAll(first.execute(pricing, time))
            if (first.state.status.aborted) state.copy(time, first.state.status)
        }

        if (first.state.status == OrderStatus.COMPLETED) {
            result.addAll(second.execute(pricing, time))
            state = state.copy(time, second.state.status)
        }

        return result
    }
}


internal class BracketOrderHandler(order: BracketOrder) : TradeOrderHandler{

    override var state: OrderState = OrderState(order)

    private val main = ExecutionEngine.getHandler(order.entry) as SingleOrderHandler<*>
    private val profit = ExecutionEngine.getHandler(order.takeProfit) as SingleOrderHandler<*>
    private val loss = ExecutionEngine.getHandler(order.stopLoss) as SingleOrderHandler<*>

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        state = state.copy(time)
        if (main.state.status.open) return main.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        val remaining = main.qty + loss.fill + profit.fill
        if (remaining.iszero) state = state.copy(time, OrderStatus.COMPLETED)
        return executions
    }

}
