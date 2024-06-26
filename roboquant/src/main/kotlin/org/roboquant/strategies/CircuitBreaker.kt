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

package org.roboquant.strategies

import org.roboquant.brokers.Account
import org.roboquant.common.Logging
import org.roboquant.common.TimeSpan
import org.roboquant.common.minus
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import java.time.Instant
import java.util.*

/**
 * Wraps another [Strategy] and based on the configured settings throttle the propagation of orders to the broker.
 * The logic enforces that all orders send at the same time will be trottled or not.
 *
 * @property strategy the underlying strategy
 * @constructor Create new Circuit Breaker
 */
internal class CircuitBreaker(val strategy: Strategy, private val maxOrders: Int, private val period: TimeSpan) : Strategy {

    private val history = LinkedList<Pair<Instant, Int>>()
    private val logger = Logging.getLogger(this::class)

    private fun exceeds(newOrders: Int, time: Instant): Boolean {
        if (newOrders > maxOrders) return true
        val lookbackTime = time - period
        var orders = newOrders
        for (entry in history) {
            if (entry.first < lookbackTime) return false
            orders += entry.second
            if (orders > maxOrders) return true
        }
        return false
    }

    override fun create(event: Event, account: Account): List<Instruction> {
        val orders = strategy.create(event,account)
        if (orders.isEmpty()) return emptyList()

        return if (exceeds(orders.size, event.time)) {
            logger.info { "trottling orders, not sending ${orders.size} orders" }
            emptyList()
        } else {
            history.addFirst(Pair(event.time, orders.size))
            orders
        }
    }

}

/**
 * Limit the number of orders a signalConverter can generate to [maxOrders] per [period]. All the orders per step will be
 * either added or ignored.
 *
 * Note: this circuit breaker will also block closing-position orders if the [maxOrders] limit is exceeded.
 *
 * Usage:
 * ```
 * // For example allow maximum of 5 orders per 8 hours
 * val signalConverter = myPolicy.circuitBreaker(5, 8.hours)
 * ```
 */
fun Strategy.circuitBreaker(maxOrders: Int, period: TimeSpan): Strategy = CircuitBreaker(this, maxOrders, period)
