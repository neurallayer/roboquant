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
import org.roboquant.common.Logging
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Uses the [Logger] under the hood to log metric results, It will log everything by default at
 * [Level.INFO] level, but this can be configured
 *
 * @constructor Create new Info logger
 */
class InfoLogger(
    private val splitMetrics: Boolean = false,
    private val level: Level = Level.INFO
) : MetricsLogger {

    private val logger = Logging.getLogger(InfoLogger::class)

    override fun log(results: Map<String, Number>, info: RunInfo) {
        if (results.isEmpty()) return

        if (!splitMetrics)
            logger.log(level) {
                mapOf(
                    "name" to info.roboquant,
                    "run" to info.run,
                    "epoch" to info.episode,
                    "time" to info.time,
                    "step" to info.step,
                    "metrics" to results
                ).toString()
            }
        else
            results.forEach {
                logger.log(level) {
                    mapOf(
                        "name" to info.roboquant,
                        "run" to info.run,
                        "epoch" to info.episode,
                        "time" to info.time,
                        "step" to info.step,
                        "name" to it.key,
                        "value" to it.value
                    ).toString()
                }
            }
    }

}