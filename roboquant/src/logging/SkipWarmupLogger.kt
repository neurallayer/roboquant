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

package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.metrics.MetricResults

/**
 * This logger wraps another [MetricsLogger] and allows to configure a number of steps at the start of a run at which
 * metrics will not be logged. The most common use case is that there is a warm-up period for a strategy that you
 * don't want to capture metrics at that period.
 *
 * @property logger the other metrics logger to wrap
 * @property minSteps the number of steps before start invoking the wrapped logger
 *
 * @constructor Create new Skip warmup logger
 */
class SkipWarmupLogger(private val logger: MetricsLogger, private val minSteps: Int = -1) : MetricsLogger by logger {

    override fun log(results: MetricResults, info: RunInfo) {
        if (info.step < minSteps) return
        logger.log(results, info)
    }

}

/**
 * Extension function for all metrics loggers that make it convenient to don't log the first [steps]
 */
fun MetricsLogger.skipFirst(steps: Int) = SkipWarmupLogger(this, steps)
