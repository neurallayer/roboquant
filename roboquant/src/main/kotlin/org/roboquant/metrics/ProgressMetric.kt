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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Metric that capture the progress of the run. The captured values are the total since
 * the start of a phase. The following metrics are captured:
 *
 * - `progress.actions`: The number of actions
 * - `progress.steps`: The number of steps (or events)
 * - `progress.walltime`: The total wall time in milliseconds
 */
class ProgressMetric : Metric {

    private var startTime = Instant.now().toEpochMilli()
    private var actions = 0
    private var steps = 0

    override fun calculate(account: Account, event: Event): MetricResults {
        actions += event.actions.size
        return metricResultsOf(
            "progress.actions" to actions,
            "progress.steps" to ++steps,
            "progress.walltime" to (Instant.now().toEpochMilli() - startTime),
        )
    }

    override fun reset() {
        startTime = Instant.now().toEpochMilli()
        actions = 0
        steps = 0
    }

}