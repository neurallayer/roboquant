package org.roboquant.brokers.sim

import org.roboquant.common.days
import org.roboquant.common.iszero
import org.roboquant.orders.*
import java.time.Instant

abstract class SingleOrderCommand<T : SingleOrder>(order: T) : OrderCommand<T>(order) {

    internal var fill = 0.0
    internal var qty = order.quantity

    val remaining
        get() = qty - fill

    var time: Instant = Instant.MIN


    /**
     * Validate TiF
     *
     * @return
     */
    private val expired: Boolean
        get() {
            return when (val tif = order.tif) {
                is GTC -> time > state.placed + tif.maxDays.days
                is DAY -> ! order.asset.exchange.sameDay(state.placed, time)
                is FOK -> remaining != 0.0
                else -> throw Exception("Unsupported TIF $tif")
            }
        }




    override fun execute(pricing: Pricing, time: Instant): List<Execution> {


        val execution = fill(pricing)
        fill += execution?.quantity ?: 0.0

        if (expired) {
            status = OrderStatus.EXPIRED
            return listOf()
        }

        if (remaining.iszero) status = OrderStatus.COMPLETED
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

