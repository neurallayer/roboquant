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

package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.metrics.MetricResults

/**
 * Silent logger ignores all metrics results and only counts the number of invocations.
 * Used mainly during unit tests to suppress the output or memory usage of logging.
 */
class SilentLogger : MetricsLogger {

    /**
     * how many events have been received during this run
     */
    var events = 0L
        private set

    override fun log(results: MetricResults, info: RunInfo) {
        events += 1
    }

    override fun reset() {
        events = 0L
    }

}
