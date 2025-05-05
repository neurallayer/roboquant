/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.metrics

import org.roboquant.common.Account
import org.roboquant.common.Event
import java.time.Instant

/**
 * This metric captures the progress of a run. The captured values are the totals since the start of a run. The
 * following metrics are captured:
 *
 * - `progress.events`: The number of events
 * - `progress.actions`: The number of actions (like candlesticks)
 * - `progress.walltime`: The total wall time in milliseconds
 */
class ProgressMetric : Metric {

    private var startTime = Instant.now().toEpochMilli()
    private var actions = 0L
    private var events = 0L


    override fun calculate(event: Event, account: Account): Map<String, Double> {
        actions += event.items.size

        return metricResultsOf(
            "progress.actions" to actions,
            "progress.events" to ++events,
            "progress.walltime" to (Instant.now().toEpochMilli() - startTime),
        )
    }

    override fun reset() {
        startTime = Instant.now().toEpochMilli()
        actions = 0
        events = 0
    }

}
