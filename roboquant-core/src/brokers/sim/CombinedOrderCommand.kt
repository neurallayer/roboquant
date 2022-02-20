package org.roboquant.brokers.sim

import org.roboquant.common.iszero
import org.roboquant.orders.BracketOrder
import org.roboquant.orders.OneCancelsOtherOrder
import org.roboquant.orders.OneTriggersOtherOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant

internal class OCOOrderCommand(order: OneCancelsOtherOrder) : OrderCommand<OneCancelsOtherOrder>(order) {

    private val first = ExecutionEngine.getOrderCommand(order.first)
    private val second = ExecutionEngine.getOrderCommand(order.second)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
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


internal class OTOOrderCommand(order: OneTriggersOtherOrder) : OrderCommand<OneTriggersOtherOrder>(order) {

    private val first = ExecutionEngine.getOrderCommand(order.first)
    private val second = ExecutionEngine.getOrderCommand(order.second)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
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


internal class BracketOrderCommand(order: BracketOrder) : OrderCommand<BracketOrder>(order) {

    private val main = ExecutionEngine.getOrderCommand(order.entry) as SingleOrderCommand<*>
    private val profit = ExecutionEngine.getOrderCommand(order.takeProfit) as SingleOrderCommand<*>
    private val loss = ExecutionEngine.getOrderCommand(order.stopLoss) as SingleOrderCommand<*>

    var fill = 0.0

    val remaining
        get() = main.qty + fill

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        if (main.status.open) return main.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        fill = loss.fill + profit.fill
        if (remaining.iszero) status = OrderStatus.COMPLETED
        return executions

    }

}
