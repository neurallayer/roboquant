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

import org.roboquant.RunInfo
import org.roboquant.metrics.MetricResults
import java.text.DecimalFormat
import java.time.temporal.ChronoUnit

/**
 * Output metric results to the console using plain println. This works with Notebooks and standalone applications.
 * By default, it will log all metrics of a step to a single line, but by setting [splitMetrics] to true, every metric
 * will be logged to a separate line.
 *
 * @see [InfoLogger]
 *
 * @property splitMetrics should every metric have its own output line, default is false
 * @property precision the precision of the time displayed, default is [ChronoUnit.SECONDS]
 */
open class ConsoleLogger(
    private val splitMetrics: Boolean = false,
    private val precision: ChronoUnit = ChronoUnit.SECONDS
) : MetricsLogger {

    private val formatter = DecimalFormat("###,###,###,###,###.####")
    private fun Map<*, *>.pretty() = toString().removePrefix("{").removeSuffix("}")

    private fun MetricResults.format(): Map<String, String> {
        return mapValues {
            formatter.format(it.value)
        }
    }

    protected fun getLines(results: MetricResults, info: RunInfo): List<String> {
        val time = info.time.truncatedTo(precision)
        return if (!splitMetrics)
            listOf(
                mapOf(
                    "run" to info.run,
                    "time" to time
                ).pretty() + ", " + results.format().pretty()
            )
        else
            results.map {
                mapOf(
                    "run" to info.run,
                    "time" to time,
                    it.key to formatter.format(it.value)
                ).pretty()
            }
    }

    override fun log(results: MetricResults, info: RunInfo) {
        if (results.isEmpty()) return
        for (line in getLines(results, info)) println(line)
    }

}
