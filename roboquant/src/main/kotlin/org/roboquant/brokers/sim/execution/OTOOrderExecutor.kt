package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.sim.Pricing
import org.roboquant.orders.OTOOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant

internal class OTOOrderExecutor(override val order: OTOOrder) : CreateOrderExecutor<OTOOrder> {

    override var status: OrderStatus = OrderStatus.INITIAL

    private val first = ExecutionEngine.getCreateOrderExecutor(order.primary)
    private val second = ExecutionEngine.getCreateOrderExecutor(order.secondary)

    /**
     * Cancel the order, return true if successful, false otherwise
     */
    override fun cancel(time: Instant) : Boolean {
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