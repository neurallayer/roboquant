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

import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Standard set of metric values that capture the progress of the run. The captured metrics are aggregated since
 * the start of a phase.
 *
 * The following metrics are captured:
 *
 * - The number of actions
 * - The number of events (or steps)
 * - The number of trades
 * - The number of orders
 * - The wall time
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
            "progress.orders" to account.orders.size,
            "progress.wallTime" to (Instant.now().toEpochMilli() - startTime.toEpochMilli()),
        )
    }

    override fun start(runPhase: RunPhase) {
        startTime = Instant.now()
        actions = 0
        steps = 0
    }

}