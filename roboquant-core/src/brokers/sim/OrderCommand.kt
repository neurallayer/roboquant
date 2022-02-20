package org.roboquant.brokers.sim

import org.roboquant.common.iszero
import org.roboquant.orders.*
import java.time.Instant





interface OrderCommand {

    fun execute(pricing: Pricing, time: Instant): List<Execution>

    val order: Order

}


abstract class SingleOrderCommand<T : SingleOrder>(final override var order: T) : OrderCommand {

    internal var fill = 0.0
    internal var qty = order.quantity

    val remaining
        get() = qty - fill

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {

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
    abstract fun fill(pricing: Pricing): Execution?
}




internal class MarketOrderCommand(order: MarketOrder) : SingleOrderCommand<MarketOrder>(order) {
    override fun fill(pricing: Pricing): Execution = Execution(order, remaining, pricing.marketPrice(remaining))
}


internal class LimitOrderCommand(order: LimitOrder) : SingleOrderCommand<LimitOrder>(order) {

    override fun fill(pricing: Pricing): Execution? {
        return if (limitTrigger(order.limit, remaining, pricing)) {
            Execution(order, remaining, order.limit)
        } else {
            null
        }
    }

}


internal class OCOOrderCommand(override val order: OneCancelsOtherOrder) : OrderCommand {

    private val first = ExecutionEngine.getTradeOrderCommand(order.first)
    private val second = ExecutionEngine.getTradeOrderCommand(order.second)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
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


internal class OTOOrderCommand(override val order: OneTriggersOtherOrder) : OrderCommand {

    private val first = ExecutionEngine.getTradeOrderCommand(order.first)
    private val second = ExecutionEngine.getTradeOrderCommand(order.second)

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
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

internal class BracketOrderCommand(override val order: BracketOrder) : OrderCommand {

    private val main = ExecutionEngine.getTradeOrderCommand(order.entry) as SingleOrderCommand<*>
    private val profit = ExecutionEngine.getTradeOrderCommand(order.takeProfit) as SingleOrderCommand<*>
    private val loss = ExecutionEngine.getTradeOrderCommand(order.stopLoss) as SingleOrderCommand<*>

    var fill = 0.0

    val remaining
        get() = main.qty + fill

    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        if (main.order.status.open) return main.execute(pricing, time)

        val executions = mutableListOf<Execution>()

        if (loss.fill.iszero) executions.addAll(profit.execute(pricing, time))
        if (profit.fill.iszero) executions.addAll(loss.execute(pricing, time))

        fill = loss.fill + profit.fill
        if (remaining.iszero) order.status = OrderStatus.COMPLETED
        return executions

    }

}


internal class StopOrderCommand(order: StopOrder) : SingleOrderCommand<StopOrder>(order) {

    override fun fill(pricing: Pricing): Execution? {
        if (stopTrigger(order.stop, remaining, pricing)) return Execution(
            order, remaining, pricing.marketPrice(remaining)
        )
        return null
    }

}


internal class StopLimitOrderCommand(order: StopLimitOrder) : SingleOrderCommand<StopLimitOrder>(order) {

    private var stopTriggered = false

    override fun fill(pricing: Pricing): Execution? {
        if (!stopTriggered) stopTriggered = stopTrigger(order.stop, remaining, pricing)
        if (stopTriggered && limitTrigger(order.limit, remaining, pricing)) return Execution(
            order,
            remaining,
            order.limit
        )

        return null
    }

}


private fun stopTrigger(stop: Double, volume: Double, pricing: Pricing): Boolean {
    return if (volume < 0.0) pricing.lowPrice(volume) <= stop
    else pricing.highPrice(volume) >= stop
}


private fun limitTrigger(limit: Double, volume: Double, pricing: Pricing): Boolean {
    return if (volume < 0.0) pricing.highPrice(volume) >= limit
    else pricing.lowPrice(volume) <= limit
}


private fun getTrailStop(oldStop: Double, trail: Double, volume: Double, pricing: Pricing): Double {

    return if (volume < 0.0) {
        // Sell stop
        val price = pricing.highPrice(volume)
        val newStop = price * (1.0 - trail)
        if (oldStop.isNaN()  || newStop > oldStop) newStop else oldStop
    } else {
        // Buy stop
        val price = pricing.lowPrice(volume)
        val newStop = price * (1.0 + trail)
        if (oldStop.isNaN() || newStop < oldStop) newStop else oldStop
    }

}


internal class TrailOrderCommand(order: TrailOrder) : SingleOrderCommand<TrailOrder>(order) {

    private var stop = Double.NaN

    override fun fill(pricing: Pricing): Execution? {
        stop = getTrailStop(stop, order.trailPercentage, remaining, pricing)
        return if (stopTrigger(stop, remaining, pricing)) Execution(
            order, remaining, pricing.marketPrice(remaining)
        ) else null
    }

}


internal class TrailLimitOrderCommand(order: TrailLimitOrder) : SingleOrderCommand<TrailLimitOrder>(order) {

    private var stop: Double = Double.NaN
    private var stopTriggered = false

    override fun fill(pricing: Pricing): Execution? {
        stop = getTrailStop(stop, order.trailPercentage, remaining, pricing)
        if (! stopTriggered) stopTriggered = stopTrigger(stop, remaining, pricing)
        val limit = stop + order.limitOffset
        return if (stopTriggered && limitTrigger(limit, remaining, pricing)) Execution(order, remaining, limit)
        else null
    }

}

