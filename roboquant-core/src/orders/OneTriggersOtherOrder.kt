package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import java.lang.Double.max
import java.time.Instant

/**
 * Creates an OTO (One Triggers Other) order. If one order is completed, the other order will be triggered.
 *
 * @constructor
 *
 */
class OneTriggersOtherOrder(
    val first: SingleOrder,
    val second: SingleOrder,
) : Order(first.asset) {

    init {
        require(first.asset == second.asset) { "Assets need to be the same" }
    }

    override fun clone(): OneCancelsOtherOrder {
        return OneCancelsOtherOrder(first.clone(), second.clone())
    }

    override fun getValue(price: Double): Double {
        return max(first.getValue(price), 0.0) + max(second.getValue(price), 0.0)
    }

    override fun execute(price: Double, now: Instant) : List<Execution> {
        val executions = mutableListOf<Execution>()
        place(price, now)

        if (first.status.open) {
            executions.addAll(first.execute(price, now))
            if (first.status.aborted) status = first.status
        }

        if (first.status == OrderStatus.COMPLETED) {
            executions.addAll(second.execute(price, now))
            status = second.status
        }

        return executions
    }
}