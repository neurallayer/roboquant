package org.roboquant.brokers.sim

import org.roboquant.orders.OrderState
import org.roboquant.common.UnsupportedException
import org.roboquant.common.days
import org.roboquant.common.iszero
import org.roboquant.orders.*
import java.time.Instant

abstract class SingleOrderHandler<T : SingleOrder>(var order: T) : TradeOrderHandler {

    var fill = 0.0
    var qty = order.quantity

    val remaining
        get() = qty - fill

    override var state: OrderState = OrderState(order)

    /**
     * Validate TiF policy and return true if order has expired according to the policy.
     */
    private fun expired(time: Instant): Boolean {
        return when (val tif = order.tif) {
            is GTC -> time > state.openedAt + tif.maxDays.days
            is DAY -> ! order.asset.exchange.sameDay(state.openedAt, time)
            is FOK -> remaining != 0.0
            is GTD -> time > tif.date
            is IOC -> time > state.openedAt
            else -> throw UnsupportedException("Unsupported TIF $tif")
        }
    }


    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        state = state.copy(time)
        val execution = fill(pricing)
        fill += execution?.quantity ?: 0.0

        if (expired(time)) {
            state = state.copy(time, OrderStatus.EXPIRED)
            return emptyList()
        }

        if (remaining.iszero) state = state.copy(time, OrderStatus.COMPLETED)
        return if (execution == null) emptyList() else listOf(execution)

    }

    /**
     * Subclasses only need to implement this method
     */
    abstract fun fill(pricing: Pricing): Execution?
}

internal class MarketOrderHandler(order: MarketOrder) : SingleOrderHandler<MarketOrder>(order) {
    override fun fill(pricing: Pricing): Execution = Execution(order, remaining, pricing.marketPrice(remaining))
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



internal class LimitOrderHandler(order: LimitOrder) : SingleOrderHandler<LimitOrder>(order) {

    override fun fill(pricing: Pricing): Execution? {
        return if (limitTrigger(order.limit, remaining, pricing)) {
            Execution(order, remaining, order.limit)
        } else {
            null
        }
    }

}

internal class StopOrderHandler(order: StopOrder) : SingleOrderHandler<StopOrder>(order) {

    override fun fill(pricing: Pricing): Execution? {
        if (stopTrigger(order.stop, remaining, pricing)) return Execution(
            order, remaining, pricing.marketPrice(remaining)
        )
        return null
    }

}


internal class StopLimitOrderHandler(order: StopLimitOrder) : SingleOrderHandler<StopLimitOrder>(order) {

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



internal class TrailOrderHandler(order: TrailOrder) : SingleOrderHandler<TrailOrder>(order) {

    private var stop = Double.NaN

    override fun fill(pricing: Pricing): Execution? {
        stop = getTrailStop(stop, order.trailPercentage, remaining, pricing)
        return if (stopTrigger(stop, remaining, pricing)) Execution(
            order, remaining, pricing.marketPrice(remaining)
        ) else null
    }

}

internal class TrailLimitOrderHandler(order: TrailLimitOrder) : SingleOrderHandler<TrailLimitOrder>(order) {

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

