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

package org.roboquant.loggers

import org.roboquant.RunInfo
import org.roboquant.common.Logging
import org.roboquant.metrics.MetricResults
import java.time.temporal.ChronoUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Uses the [Logger] under the hood to log metric results, It will log everything by default at
 * [Level.INFO] level.
 *
 * @property splitMetrics should every metric have its own logging line, default is false
 * @property precision the precision of the time output, default is [ChronoUnit.SECONDS]
 *
 * @constructor Create new Info logger
 */
class InfoLogger(
    private val splitMetrics: Boolean = false,
    private val precision: ChronoUnit = ChronoUnit.SECONDS
) : ConsoleLogger(splitMetrics, precision) {

    private val logger = Logging.getLogger(InfoLogger::class)

    override fun log(results: MetricResults, info: RunInfo) {
        if (logger.isInfoEnabled) for (line in getLines(results, info)) logger.info(line)
    }

}