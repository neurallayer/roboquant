/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers.sim.execution

import org.roboquant.common.Size
import org.roboquant.common.UnsupportedException
import org.roboquant.common.days
import org.roboquant.common.plus
import org.roboquant.feeds.PriceItem
import org.roboquant.orders.*
import java.time.Instant

/**
 * Base class for executing single orders. It takes care of Time-In-Force handling and status management and makes
 * the implementation of concrete single order executors like a MarketOrder easier.
 *
 * @property order the single order to execute
 */
abstract class SingleOrderExecutor<T : SingleOrder>(final override var order: T) : OrderExecutor {

    /**
     * Fill so far
     */
    var fill = Size.ZERO
        internal set

    protected var trigger: Double? = null
    protected var triggered = false

    protected var limit: Double? = null

    /**
     * Remaining order size
     */
    private val remaining
        get() = order.size - fill

    // Used by time-in-force policies
    private val openedAt: Instant
        get() = order.openedAt

    /**
     * Cancel the order, return true if successful, false otherwise. Any open SingleOrder can be cancelled, closed
     * orders not.
     */
    override fun cancel(time: Instant): Boolean {
        if (status == OrderStatus.ACCEPTED && expired(time)) status = OrderStatus.EXPIRED
        if (status.closed) return false
        status = OrderStatus.CANCELLED
        return true
    }

    /**
     * Validate TiF trader and return true if the order has expired according to the defined TIF.
     */
    open fun expired(time: Instant): Boolean {
        return when (val tif = order.tif) {
            is GTC -> time > (openedAt + tif.maxDays.days)
            is DAY -> (time.epochSecond - order.openedAt.epochSecond) >= 3600 * 24 // approximation
            is FOK -> remaining.nonzero
            is GTD -> time > tif.date
            is IOC -> time > openedAt
            else -> throw UnsupportedException("unsupported time-in-force trader tif=$tif")
        }
    }


    open fun isTriggered(price: Double): Boolean {
        val trigger = trigger ?: return true
        return when {
            order.buy && price >= trigger -> true
            order.sell && price <= trigger -> true
            else -> false
        }
    }

    open fun reachedLimit(price: Double): Boolean {
        val limit = limit ?: return true
        return when {
            order.buy && price <= limit -> true
            order.sell && price >= limit -> true
            else -> false
        }
    }

    /**
     * Execute the order, using the provided [item] and [time] as input.
     */
    override fun execute(item: PriceItem, time: Instant): List<Execution> {
        if (status == OrderStatus.CREATED) {
            status = OrderStatus.ACCEPTED
            order.openedAt = time
        }

        val price = item.getPrice()
        if (!triggered && isTriggered(price)) triggered = true

        val newFill = if (triggered && reachedLimit(price)) {
            remaining
        } else {
            Size.ZERO
        }

        fill += newFill

        if (expired(time)) {
            status = OrderStatus.EXPIRED
            return emptyList()
        }

        if (remaining.iszero) status = OrderStatus.COMPLETED
        return if (newFill.iszero) emptyList() else listOf(Execution(order, newFill, price))
    }

    @Suppress("ReturnCount")
    fun update(order: Order, time: Instant): Boolean {
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

    override fun modify(modification: Modification, time: Instant): Boolean {
        return update(modification.update, time)
    }


}


internal class MarketOrderExecutor(order: MarketOrder) : SingleOrderExecutor<MarketOrder>(order)

internal class LimitOrderExecutor(order: LimitOrder) : SingleOrderExecutor<LimitOrder>(order) {

    init {
        limit = order.limit
    }

}

internal class StopOrderExecutor(order: StopOrder) : SingleOrderExecutor<StopOrder>(order) {

    init {
        trigger = order.stop
    }
}

internal class StopLimitOrderExecutor(order: StopLimitOrder) : SingleOrderExecutor<StopLimitOrder>(order) {

    init {
        limit = order.limit
        trigger = order.stop
    }

}

private fun determineTrail(buy: Boolean, item: PriceItem, oldTrail: Double?, perc: Double): Double {
    val price = item.getPrice()
    val newTrail = if (buy) price * (1.0 + perc) else price * (1.0 - perc)
    return when {
        oldTrail == null -> newTrail
        buy && newTrail < oldTrail -> newTrail
        !buy && newTrail > oldTrail -> newTrail
        else -> oldTrail
    }
}


internal class TrailOrderExecutor(order: TrailOrder) : SingleOrderExecutor<TrailOrder>(order) {

    override fun execute(item: PriceItem, time: Instant): List<Execution> {
        if (!triggered) {
            trigger = determineTrail(order.buy, item, trigger, order.trailPercentage)
        }
        return super.execute(item, time)
    }

}

internal class TrailLimitOrderExecutor(order: TrailLimitOrder) : SingleOrderExecutor<TrailLimitOrder>(order) {

    override fun execute(item: PriceItem, time: Instant): List<Execution> {
        if (!triggered) {
            trigger = determineTrail(order.buy, item, trigger, order.trailPercentage)
            limit = trigger!! + order.limitOffset
        }
        return super.execute(item, time)
    }


}

