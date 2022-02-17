package org.roboquant.brokers.sim

import org.roboquant.common.iszero
import org.roboquant.orders.*
import java.time.Instant


interface ModifyOrderCommand {

    fun execute(price: Double, time: Instant): Double

    val order: Order
}



interface OrderCommand {

    fun execute(pricing: Pricing, time: Instant) : List<Execution>

    val order: Order

}

abstract class SingleOrderCommand<T: SingleOrder>(final override val order: T)  : OrderCommand {

    internal var fill = 0.0
    internal var qty = order.quantity

    val remaining
        get() = qty - fill

    override fun execute(pricing: Pricing, time: Instant) : List<Execution> {
        val execution = fill(pricing)
        fill += execution?.quantity ?: 0.0

        if (order.tif.isExpired(order, time, remaining)) {
            order.status = OrderStatus.EXPIRED
            return listOf()
        }

        if (remaining.iszero) order.status = OrderStatus.COMPLETED
        return if (execution == null) listOf() else listOf(execution)

    }

    /**
     * Subclasses only need to implement this method
     */
    abstract fun fill(pricing: Pricing) : Execution?
}

class MarketOrderCommand(order: MarketOrder) : SingleOrderCommand<MarketOrder>(order) {
    override fun fill(pricing: Pricing) : Execution = Execution(order, remaining, pricing.marketPrice(remaining))
}


class LimitOrderCommand(order: LimitOrder) : SingleOrderCommand<LimitOrder>(order) {

    override fun fill(pricing: Pricing) : Execution? {
        if (order.buy && pricing.lowPrice(remaining) <= order.limit) return Execution(order, remaining, order.limit)
        if (order.sell && pricing.highPrice(remaining) >= order.limit) return Execution(order, remaining, order.limit)
        return null
    }

}



class OCOOrderCommand(override val order: OneCancelsOtherOrder) : OrderCommand {

    private val first = ExecutionEngine.getOrderCommand(order.first)
    private val second = ExecutionEngine.getOrderCommand(order.second)

    override fun execute(pricing: Pricing, time: Instant) : List<Execution> {
        val result = mutableListOf<Execution>()

        if (first.order.status.open) {
            result.addAll(first.execute(pricing, time))
            if (first.order.status.aborted) order.status = first.order.status
        }

        if (first.order.status == OrderStatus.COMPLETED) {
            result.addAll(second.execute(pricing, time))
            order.status = second.order.status
        }

        return result
    }
}


class BracketOrderCommand(override val order: BracketOrder) : OrderCommand {

    private val main = ExecutionEngine.getOrderCommand(order.main) as SingleOrderCommand<*>
    val profit = ExecutionEngine.getOrderCommand(order.profit) as SingleOrderCommand<*>
    val loss = ExecutionEngine.getOrderCommand(order.loss) as SingleOrderCommand<*>

    var fill = 0.0

    val remaining
        get() = main.qty + fill

    override fun execute(pricing: Pricing, time: Instant) : List<Execution>  {
        if (main.order.status.open) return main.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        fill = loss.fill + profit.fill
        if (remaining.iszero) order.status = OrderStatus.COMPLETED
        return executions

    }


}



class StopOrderCommand(order: StopOrder) : SingleOrderCommand<StopOrder>(order) {

    override fun fill(pricing: Pricing) : Execution? {
        return if ((order.sell && pricing.lowPrice(remaining) <= order.stop) || (order.buy && pricing.highPrice(remaining) >= order.stop)) {
            Execution(order, remaining, pricing.marketPrice(remaining))
        } else {
            null
        }

    }

}


private fun isStopTriggered(order: StopOrder, volume: Double, pricing: Pricing) : Boolean {
    return (order.sell && pricing.lowPrice(volume) <= order.stop) || (order.buy && pricing.highPrice(volume) >= order.stop)
}


private fun isLimitTriggered(order: LimitOrder, volume: Double, pricing: Pricing) : Boolean {
    return (order.sell && pricing.lowPrice(volume) <= order.limit) || (order.buy && pricing.highPrice(volume) >= order.limit)
}



class StopLimitOrderCommand(order: StopLimitOrder) : SingleOrderCommand<StopLimitOrder>(order) {

    private var triggered = false

    override fun fill(pricing: Pricing) : Execution? {

        if (!triggered) triggered = isStopTriggered(order, remaining, pricing)

        if (triggered) {
            if ((order.sell && pricing.lowPrice(remaining) <= order.limit) || (order.buy && pricing.highPrice(remaining) >= order.limit)) {
                return Execution(order, remaining, order.limit)
            }
        }

        return null
    }

}
