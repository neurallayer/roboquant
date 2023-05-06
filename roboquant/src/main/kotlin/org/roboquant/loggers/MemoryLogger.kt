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
import org.roboquant.RunPhase
import org.roboquant.common.Summary
import org.roboquant.metrics.MetricResults
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Store metric results in memory. Very convenient in a Jupyter notebook when you want to inspect or visualize
 *  metric results after a run. This is also the default metric logger for roboquant if none is specified.
 *
 * If you log very large amounts of metric data, this could cause memory issues. But for normal back testing
 * this should not pose a problem. By specifying [maxHistorySize] you can further limit the amount stored in memory.
 *
 * By default, a bar will be shown that shows the progress of a run, but by setting [showProgress] to false this can be
 * disabled. And although the MemoryLogger can be used in multiple runs at the same time, that is not true for the
 * progressbar.
 *
 * @property showProgress should a progress bar be displayed, default is true
 */
class MemoryLogger(var showProgress: Boolean = true, private val maxHistorySize: Int = Int.MAX_VALUE) : MetricsLogger {

    internal val history = mutableListOf<Pair<MetricResults, RunInfo>>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: MetricResults, info: RunInfo) {
        if (showProgress) progressBar.update(info)
        if (results.isEmpty()) return
        if (history.size >= maxHistorySize) history.removeFirst()
        history.add(Pair(results, info))
    }

    override fun start(runPhase: RunPhase) {
        if (showProgress) progressBar.reset()
    }

    override fun end(runPhase: RunPhase) {
        if (showProgress) progressBar.done()
    }

    /**
     * Clear the history that is kept in the logger
     */
    override fun reset() {
        history.clear()
        progressBar.reset()
    }

    /**
     * Provided a summary of the recorded metrics for [last] events (default is 1)
     */
    fun summary(last: Int = 1): Summary {
        val s = Summary("Metrics")
        val lastEntry = history.lastOrNull()
        if (lastEntry == null) {
            s.add("No Data")
        } else {
            for (i in history.lastIndex downTo history.lastIndex - last) {
                if (i >= 0) {
                    val step = history[i]
                    val t = Summary("${step.second.time}")
                    for (entry in step.first) {
                        t.add(entry.key, entry.value)
                    }
                    s.add(t)
                }
            }
        }
        return s
    }

    /**
     * Return the recorded run phases
     */
    val runPhases: List<Pair<String, RunPhase>>
        get() = history.map { Pair(it.second.run, it.second.phase) }.distinct().sortedBy { it.first }

    /**
     * Get all the recorded runs in this logger
     */
    val runs
        get() = history.map { it.second.run }.distinct().sorted()

    /**
     * Get the unique list of metric names that have been captured
     */
    override val metricNames: List<String>
        get() = history.map { it.first.keys }.flatten().distinct().sorted()

    /**
     * Get results for a metric specified by its [name]. It will include all the runs for that metric.
     */
    override fun getMetric(name: String): List<MetricsEntry> {
        val result = mutableListOf<MetricsEntry>()
        for (entry in history) {
            val value = entry.first[name]
            if (value != null) result.add(MetricsEntry(name, value, entry.second))
        }
        return result
    }

}

/**
 * Group a collection of metric entries by a certain [period]. This for example enables to group by month and run
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
        val date = Date.from(it.info.time)
        formatter.format(date)
    }
}

/**
 * Convert a collection of metric entries into a double array.
 */
fun Collection<MetricsEntry>.toDoubleArray() = map { it.value }.toDoubleArray()

/**
 * Generate a name for a collection of metric entries
 */
fun Collection<MetricsEntry>.getName(): String {
    return map { it.name }.distinct().joinToString("/") { it.replace('.', ' ') }
}