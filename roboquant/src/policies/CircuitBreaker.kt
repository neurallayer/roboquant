/*
 * Copyright 2022 Neural Layer
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
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant
import java.time.temporal.TemporalAmount
import java.util.*

/**
 * Wraps another [policy] and based on tje configured throttle settings stops propagating orders to a broker.
 *
 * Usage:
 *      // For example maximum of 5 orders per 8 hours
 *      val policy = ChainBreaker(DefaultPolicy(), 5, 8.hours)
 *
 * @property policy
 * @constructor Create new Chain Breaker
 */
class CircuitBreaker(val policy: Policy, private val maxOrders: Int, private val duration: TemporalAmount) :
    Policy by policy {

    private val history = LinkedList<Pair<Instant, Int>>()

    private fun exceeds(newOrders: Int, time: Instant): Boolean {
        val lookbackTime = time - duration
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

fun Policy.circuitBreaker(maxOrders: Int, duration: TemporalAmount) = CircuitBreaker(this, maxOrders, duration)