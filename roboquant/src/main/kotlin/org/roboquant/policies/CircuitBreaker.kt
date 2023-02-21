/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant
import java.util.*

/**
 * Wraps another [policy] and based on the configured throttle settings stops propagating orders to a broker.
 *
 * @property policy
 * @constructor Create new Chain Breaker
 */
internal class CircuitBreaker(val policy: Policy, private val maxOrders: Int, private val period: TradingPeriod) :
    Policy by policy {

    private val history = LinkedList<Pair<Instant, Int>>()

    private fun exceeds(newOrders: Int, time: Instant): Boolean {
        val lookbackTime = time - period
        var orders = newOrders
        for (entry in history) {
            if (entry.first < lookbackTime) return false
            orders += entry.second
            if (orders > maxOrders) return true
        }
        return false
    }

    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        val orders = policy.act(signals, account, event)
        if (orders.isEmpty() || orders.size > maxOrders) return emptyList()

        return if (exceeds(orders.size, event.time)) {
            emptyList()
        } else {
            history.addFirst(Pair(event.time, orders.size))
            orders
        }
    }

    override fun reset() {
        history.clear()
        policy.reset()
    }

}

/**
 * Limit the number of orders a policy can generate to [maxOrders] per [period]. All the orders per step will be
 * either added or ignored.
 *
 * Note: this circuit breaker will also block closing-position orders if a [maxOrders] limit is exceeded.
 *
 * Usage:
 *      // For example allow maximum of 5 orders per 8 hours
 *      val policy = myPolicy.circuitBreaker(5, 8.hours)
 */
fun Policy.circuitBreaker(maxOrders: Int, period: TradingPeriod): Policy = CircuitBreaker(this, maxOrders, period)
