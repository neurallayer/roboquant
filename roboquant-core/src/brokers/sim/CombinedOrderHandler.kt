package org.roboquant.brokers.sim

import org.roboquant.common.iszero
import org.roboquant.orders.*
import java.time.Instant

internal class OCOOrderHandler(order: OneCancelsOtherOrder) : OrderHandler<OneCancelsOtherOrder>(order) {

    private val first = ExecutionEngine.getHandler(order.first)
    private val second = ExecutionEngine.getHandler(order.second)
    private var active = 0

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)

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
                status = first.status
                return result
            }
        }

        return emptyList()
    }
}


internal class OTOOrderHandler(order: OneTriggersOtherOrder) : OrderHandler<OneTriggersOtherOrder>(order) {

    private val first = ExecutionEngine.getHandler(order.first)
    private val second = ExecutionEngine.getHandler(order.second)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
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
}


internal class BracketOrderHandler(order: BracketOrder) : OrderHandler<BracketOrder>(order) {

    private val main = ExecutionEngine.getHandler(order.entry) as SingleOrderHandler<*>
    private val profit = ExecutionEngine.getHandler(order.takeProfit) as SingleOrderHandler<*>
    private val loss = ExecutionEngine.getHandler(order.stopLoss) as SingleOrderHandler<*>

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        update(time)
        if (main.status.open) return main.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        val remaining = main.qty + loss.fill + profit.fill
        if (remaining.iszero) close(OrderStatus.COMPLETED, time)
        return executions
    }

}
