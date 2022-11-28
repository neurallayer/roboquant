/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers.sim.execution

import org.roboquant.brokers.sim.Pricing
import org.roboquant.common.*
import org.roboquant.orders.*
import java.time.Instant

/**
 * Base class for executing single orders. It will take care of Time-In-Force handling and status management.
 *
 * @property order the single order to execute
 */
internal abstract class SingleOrderExecutor<T : SingleOrder>(final override var order: T) : CreateOrderExecutor<T> {

    /**
     * Fill size
     */
    var fill = Size.ZERO

    /**
     * Quantity
     */
    var qty = order.size

    /**
     * Remaining order size
     */
    val remaining
        get() = qty - fill

    // Used by time-in-force policies
    private lateinit var openedAt: Instant

    /**
     * Order status
     */
    override var status : OrderStatus = OrderStatus.INITIAL

    /**
     * Cancel the order, return true if successful, false otherwise. Any open SingleOrder can be cancelled, closed
     * orders not.
     */
    override fun cancel(time: Instant) : Boolean {
        return if (status.closed) {
            false
        } else {
            status = OrderStatus.CANCELLED
            true
        }
    }

    /**
     * Validate TiF policy and return true if order has expired according to the defined policy.
     */
    private fun expired(time: Instant): Boolean {
        return when (val tif = order.tif) {
            is GTC -> time > (openedAt + tif.maxDays.days)
            is DAY -> !order.asset.exchange.sameDay(openedAt, time)
            is FOK -> remaining.nonzero
            is GTD -> time > tif.date
            is IOC -> time > openedAt
            else -> throw UnsupportedException("unsupported time-in-force policy tif=$tif")
        }
    }

    /**
     * Execute the order, using the provided [pricing] and [time] as input.
     */
    override fun execute(pricing: Pricing, time: Instant): List<Execution> {
        if (status == OrderStatus.INITIAL) {
            status = OrderStatus.ACCEPTED
            openedAt = time
        }

        val execution = fill(pricing)
        fill += execution?.size ?: Size.ZERO

        if (expired(time)) {
            status = OrderStatus.EXPIRED
            return emptyList()
        }

        if (remaining.iszero) status = OrderStatus.COMPLETED
        return if (execution == null) emptyList() else listOf(execution)
    }

    @Suppress("ReturnCount")
    override fun update(order: CreateOrder, time: Instant): Boolean {
        if (status == OrderStatus.ACCEPTED && expired(time)) return false
        if (status.closed) return false

        @Suppress("UNCHECKED_CAST")
        val newOrder = order as? T
        return if (newOrder != null) {
            if (newOrder.size != order.size) return false
            this.order = newOrder
            true
        } else {
            false
        }

    }

    /**
     * Subclasses only need to implement this method and don't worry about time-in-force and state management.
     */
    abstract fun fill(pricing: Pricing): Execution?
}

internal class MarketOrderExecutor(order: MarketOrder) : SingleOrderExecutor<MarketOrder>(order) {

    /**
     * Market orders will always fill completely against the [pricing] provided. It uses [Pricing.marketPrice] to
     * get the actual price.
     */
    override fun fill(pricing: Pricing): Execution = Execution(order, remaining, pricing.marketPrice(remaining))
}

private fun stopTrigger(stop: Double, size: Size, pricing: Pricing): Boolean {
    return if (size < 0.0) pricing.lowPrice(size) <= stop
    else pricing.highPrice(size) >= stop
}

private fun limitTrigger(limit: Double, size: Size, pricing: Pricing): Boolean {
    return if (size < 0.0) pricing.highPrice(size) >= limit
    else pricing.lowPrice(size) <= limit
}

private fun getTrailStop(oldStop: Double, trail: Double, size: Size, pricing: Pricing): Double {

    return if (size < 0.0) {
        // Sell stop
        val price = pricing.highPrice(size)
        val newStop = price * (1.0 - trail)
        if (oldStop.isNaN() || newStop > oldStop) newStop else oldStop
    } else {
        // Buy stop
        val price = pricing.lowPrice(size)
        val newStop = price * (1.0 + trail)
        if (oldStop.isNaN() || newStop < oldStop) newStop else oldStop
    }

}

internal class LimitOrderExecutor(order: LimitOrder) : SingleOrderExecutor<LimitOrder>(order) {

    override fun fill(pricing: Pricing): Execution? {
        return if (limitTrigger(order.limit, remaining, pricing)) {
            Execution(order, remaining, order.limit)
        } else {
            null
        }
    }

}

internal class StopOrderExecutor(order: StopOrder) : SingleOrderExecutor<StopOrder>(order) {

    /**
     * Stop orders will fill completely only if the configured stop price is triggered.
     */
    override fun fill(pricing: Pricing): Execution? {
        if (stopTrigger(order.stop, remaining, pricing)) return Execution(
            order, remaining, pricing.marketPrice(remaining)
        )
        return null
    }

}

internal class StopLimitOrderExecutor(order: StopLimitOrder) : SingleOrderExecutor<StopLimitOrder>(order) {

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

internal class TrailOrderExecutor(order: TrailOrder) : SingleOrderExecutor<TrailOrder>(order) {

    private var stop = Double.NaN

    override fun fill(pricing: Pricing): Execution? {
        stop = getTrailStop(stop, order.trailPercentage, remaining, pricing)
        return if (stopTrigger(stop, remaining, pricing)) Execution(
            order, remaining, pricing.marketPrice(remaining)
        ) else null
    }

}

internal class TrailLimitOrderExecutor(order: TrailLimitOrder) : SingleOrderExecutor<TrailLimitOrder>(order) {

    private var stop: Double = Double.NaN
    private var stopTriggered = false

    override fun fill(pricing: Pricing): Execution? {
        stop = getTrailStop(stop, order.trailPercentage, remaining, pricing)
        if (!stopTriggered) stopTriggered = stopTrigger(stop, remaining, pricing)
        val limit = stop + order.limitOffset
        return if (stopTriggered && limitTrigger(limit, remaining, pricing)) Execution(order, remaining, limit)
        else null
    }

}

