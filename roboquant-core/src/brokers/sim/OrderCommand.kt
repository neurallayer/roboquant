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

    private val main = ExecutionEngine.getOrderCommand(order.entry) as SingleOrderCommand<*>
    val profit = ExecutionEngine.getOrderCommand(order.takeProfit) as SingleOrderCommand<*>
    val loss = ExecutionEngine.getOrderCommand(order.stopLoss) as SingleOrderCommand<*>

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
        val trigger = if (order.sell) ::sellTrigger else ::buyTrigger
        if (trigger(order.stop, remaining, pricing)) return Execution(order, remaining, pricing.marketPrice(remaining))
        return null
    }

}



private fun sellTrigger(limit: Double, volume: Double, pricing: Pricing) = pricing.lowPrice(volume) <= limit

private fun buyTrigger(limit: Double, volume: Double, pricing: Pricing) = pricing.highPrice(volume) >= limit


private fun getStop(stop: Double, trail: Double, volume: Double, pricing: Pricing) : Double {

    return if (volume > 0.0) {
        // BUY stop
        val price = pricing.highPrice(volume)
        val newStop = price * (1.0 - trail)
        if (newStop > stop) newStop else stop
    } else {
        // SELL stop
        val price = pricing.lowPrice(volume)
        val newStop = price * (1.0 + trail)
        if (newStop < stop) newStop else stop
    }

}


open class TrailOrderCommand(order: TrailOrder) : SingleOrderCommand<TrailOrder>(order) {

    protected var triggered = false
    private var stop: Double = if (order.buy) Double.MAX_VALUE else Double.MIN_VALUE
    private val correction = if (order.buy) 1.0 + order.trailPercentage else 1.0 - order.trailPercentage


    override fun fill(pricing: Pricing) : Execution? {
        stop = getStop(stop, order.trailPercentage, remaining, pricing)
        val trigger = if (order.sell) ::sellTrigger else ::buyTrigger
        return if (trigger(stop, remaining, pricing)) Execution(order, remaining, pricing.marketPrice(remaining)) else null
    }

}


class StopLimitOrderCommand(order: StopLimitOrder) : SingleOrderCommand<StopLimitOrder>(order) {

    private var triggered = false

    override fun fill(pricing: Pricing) : Execution? {
        val trigger = if (order.sell) ::sellTrigger else ::buyTrigger

        if (! triggered) triggered = trigger(order.stop, remaining, pricing)
        if (triggered && trigger(order.limit, remaining, pricing)) return Execution(order, remaining, order.limit)

        return null
    }

}
