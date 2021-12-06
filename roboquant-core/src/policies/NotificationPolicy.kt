/*
 * Copyright 2021 Neural Layer
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
import org.roboquant.common.ConsoleNotifier
import org.roboquant.common.Logging
import org.roboquant.common.Notifier
import org.roboquant.feeds.Event
import org.roboquant.orders.Order
import org.roboquant.strategies.Signal
import java.time.Instant

/**
 * Notification policy sends out notifications (like emails) when it receives one or more signals. It doesn't create any
 * orders, so it is not suited for back testing.
 *
 * You normally would use it when you want you to run your strategy against live data feed and get an alert as soon
 * as your strategy generates a signal. You can then decide what to do and perhaps manually place an order.
 *
 * @constructor Create new Notification Policy
 */
class NotificationPolicy(
    private val notifier: Notifier = ConsoleNotifier(),
    private val maxFrequencySeconds: Int = 300

) : Policy {

    private val backlog = mutableListOf<Pair<Instant, Signal>>()
    private var nextAlert: Instant = Instant.MIN
    private val logger = Logging.getLogger(this)

    /**
     * used as subject header in the email
     */
    private val subject = "roboquant: signals detected"

    /**
     * Create a string representation for a single signal
     *
     * @param signal
     * @param now
     * @return
     */
    private fun signalMsg(now: Instant, signal: Signal): String {
        val tp = if (signal.takeProfit.isNaN()) "unknown" else signal.takeProfit
        val sl = if (signal.stopLoss.isNaN()) "unknown" else signal.stopLoss
        val source = signal.source.ifEmpty { "unknown" }
        return """
            
            asset: ${signal.asset}
            time: $now
            rating: ${signal.rating}
            source: $source
            target price: $tp
            stop loss: $sl
            
        """.trimIndent()
    }


    /**
     * Act on the received signals, and create an email. Since this policy doesn't create any orders, this method
     * will always return an empty list.
     *
     * @param signals list of signals
     * @param account the account
     * @param event the step associated with this moment in time
     * @return The list of orders that will be sent to the broker
     */
    override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
        for (signal in signals) backlog.add(Pair(event.now, signal))

        val realTime = Instant.now()

        if (realTime > nextAlert && backlog.isNotEmpty()) {
            var body = "Dear Trader, \nReceived following signal(s):\n"
            for ((time, signal) in backlog) body += signalMsg(time, signal)
            try {
                notifier.send(subject, body)
            } catch (e: Exception) {
                logger.warning(e.printStackTrace().toString())
            }
            nextAlert = realTime.plusSeconds(maxFrequencySeconds.toLong())
            backlog.clear()
        }
        return listOf()
    }


}
