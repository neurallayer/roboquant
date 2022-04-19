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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Set of metrics that capture the progress of the run. The captured metrics are aggregated since
 * the start of a phase. The following metrics are captured:
 *
 * - progress.actions: The number of actions
 * - progress.events: The number of events (or steps)
 * - progress.trades: The number of trades
 * - progress.orders: The number of orders
 * - progress.walltime : The wall time
 */
class ProgressMetric : SimpleMetric() {

    private var startTime = Instant.now()
    private var actions = 0
    private var steps = 0

    override fun calc(account: Account, event: Event): MetricResults {
        actions += event.actions.size
        return mapOf(
            "progress.actions" to actions,
            "progress.events" to ++steps,
            "progress.trades" to account.trades.size,
            "progress.orders" to account.openOrders.size + account.closedOrders.size,
            "progress.walltime" to (Instant.now().toEpochMilli() - startTime.toEpochMilli()),
        )
    }

    override fun reset() {
        startTime = Instant.now()
        actions = 0
        steps = 0
    }

}