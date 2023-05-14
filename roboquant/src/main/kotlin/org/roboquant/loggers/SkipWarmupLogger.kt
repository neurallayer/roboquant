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

package org.roboquant.loggers

import org.roboquant.Run
import org.roboquant.Step
import org.roboquant.metrics.MetricResults

/**
 * This logger wraps another [MetricsLogger] and allows to configure a number of steps at the start of a run at which
 * metrics will not be logged. The most common use case is that there is a warm-up period for a strategy that you
 * don't yet want to capture metrics.
 *
 * @property logger the logger to invoke after the warmup period has finished
 * @property skip the number of steps to skip before start invoking the wrapped logger
 *
 * @constructor Create new skip warmup logger
 */
class SkipWarmupLogger(private val logger: MetricsLogger, private val skip: Int = -1) : MetricsLogger by logger {

    private var stepCounter = 0

    /**
     * @see MetricsLogger.log
     */
    override fun log(results: MetricResults, step: Step) {
        stepCounter++
        if (stepCounter <= skip) return
        logger.log(results, step)
    }

    /**
     * @see MetricsLogger.start
     */
    override fun start(run: Run) {
        stepCounter = 0
        logger.start(run)
    }

}

/**
 * Extension function for all metrics loggers that make it convenient to don't log the first [skip] steps.
 */
fun MetricsLogger.skipFirst(skip: Int) = SkipWarmupLogger(this, skip)
