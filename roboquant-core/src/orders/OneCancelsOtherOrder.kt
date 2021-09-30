package org.roboquant.orders

import org.roboquant.brokers.sim.Execution
import java.lang.Double.max
import java.time.Instant


/**
 * Creates an OCO (One Cancels Other) order. If one order is (partially) filled, the other order will be cancelled. So
 * at most only one of the two orders will be executed.
 *
 * @constructor
 *
 */
class OneCancelsOtherOrder(
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
        return max(first.getValue(price), second.getValue(price))
    }

    override fun execute(price: Double, time: Instant) : List<Execution> {
        var executions = listOf<Execution>()
        place(price, time)

        if (first.status.open) {
            executions = first.execute(price, time)
            status = first.status
            if (first.executed) second.status = OrderStatus.CANCELLED
        }

        if (second.status.open) {
            executions = second.execute(price, time)
            status = second.status
            if (second.executed) first.status = OrderStatus.CANCELLED
        }

        return executions
    }
}


