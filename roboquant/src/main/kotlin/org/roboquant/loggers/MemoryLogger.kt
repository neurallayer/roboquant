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

import org.roboquant.common.Timeframe
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Store metric results in memory. Very convenient in a Jupyter notebook when you want to inspect or visualize
 *  metric results after a run. This is also the default metric logger for roboquant if none is specified.
 *
 * If you log very large amounts of metric data, this could cause memory issues. But for normal back testing
 * this should not pose a problem.
 *
 * By default, a bar will be shown that shows the progress of a run, but by setting [showProgress] to false this can be
 * disabled. And although the MemoryLogger can be used in multiple runs at the same time, that is not true for the
 * progressbar.
 *
 * @property showProgress should a progress bar be displayed, default is true
 */
class MemoryLogger(var showProgress: Boolean = true) : MetricsLogger {

    // internal val history = mutableListOf<Pair<Map<String, Double>, Run>>()
    internal class Entry(val time: Instant, val metrics: Map<String, Double>)
    internal val history = mutableMapOf<String, MutableList<Entry>>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: Map<String, Double>, time: Instant, run: String) {
        if (showProgress) progressBar.update(time)
        if (results.isEmpty()) return
        history.getValue(run).add(Entry(time, results))
    }

    override fun start(run: String, timeframe: Timeframe) {
        if (showProgress) {
            progressBar.start(run, timeframe)
        }
        history[run] = mutableListOf()
    }

    override fun end(run: String) {
        if (showProgress) progressBar.done()
    }

    /**
     * Clear the history that is kept in the logger
     */
    override fun reset() {
        history.clear()
    }


    /**
     * Get all the recorded runs in this logger
     */
    val runs: Set<String>
        get() = history.keys

    /**
     * Get the unique list of metric names that have been captured
     */
    override val metricNames: List<String>
        get() = history.values.asSequence().flatten().map { it.metrics.keys }.flatten().distinct().sorted().toList()

    /**
     * Get results for a metric specified by its [name]. It will include all the runs for that metric.
     */
    override fun getMetric(name: String): Map<String, List<MetricsEntry>> {
        val result = mutableMapOf<String, List<MetricsEntry>>()
        for ((run, entries) in history) {
            val metrics = entries.filter { it.metrics.contains(name) }
            if (metrics.isNotEmpty()) {
                result[run] = metrics.map {
                    MetricsEntry(it.metrics.getValue(name), it.time)
                }
            }
        }
        return result
    }

}

/**
 * Group a collection of metric entries by a certain [period]. This for example enables to group them by month and run
 * statistics over each month.
 */
fun Collection<MetricsEntry>.groupBy(
    period: ChronoUnit,
    zoneId: ZoneId = ZoneOffset.UTC
): Map<String, Collection<MetricsEntry>> {

    val formatter = when (period) {
        ChronoUnit.YEARS -> SimpleDateFormat("yyyy")
        ChronoUnit.MONTHS -> SimpleDateFormat("yyyy-MM")
        ChronoUnit.WEEKS -> SimpleDateFormat("yyyy-ww")
        ChronoUnit.DAYS -> SimpleDateFormat("yyyy-DDD")
        ChronoUnit.HOURS -> SimpleDateFormat("yyyy-DDD-HH")
        ChronoUnit.MINUTES -> SimpleDateFormat("yyyy-DDD-HH-mm")
        else -> {
            throw IllegalArgumentException("Unsupported value $period")
        }
    }
    formatter.timeZone = TimeZone.getTimeZone(zoneId)
    return groupBy {
        val date = Date.from(it.time)
        formatter.format(date)
    }
}

/**
 * Convert a collection of metric entries into a double array.
 */
fun Collection<MetricsEntry>.toDoubleArray() = map { it.value }.toDoubleArray()

