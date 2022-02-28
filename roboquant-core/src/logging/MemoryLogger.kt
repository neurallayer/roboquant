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
import org.roboquant.RunPhase
import org.roboquant.common.Config
import org.roboquant.common.Summary
import org.roboquant.metrics.MetricResults
import java.text.SimpleDateFormat
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
 */
class MemoryLogger(var showProgress: Boolean = true, private val maxHistorySize: Int = Int.MAX_VALUE) : MetricsLogger {

    val history = mutableListOf<MetricsEntry>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: MetricResults, info: RunInfo) {
        if (showProgress) progressBar.update(info)

        for ((t, u) in results) {
            if (history.size >= maxHistorySize) history.removeFirst()
            val entry = MetricsEntry(t, u.toDouble(), info)
            history.add(entry)
        }
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
            val lastStep = lastEntry.info.step - last
            val map = history.filter {
                it.info.step > lastStep && lastEntry.sameEpisode(it)
            }.groupBy { it.info.time }.toSortedMap()
            for ((time, entries) in map) {
                val t = Summary("$time")
                for (entry in entries) {
                    t.add(entry.metric, entry.value)
                }
                s.add(t)
            }
        }
        return s
    }

    /**
     * Get all the recorded runs in this logger
     */
    val runs
        get() = history.map { it.info.run }.distinct().sorted()

    /**
     * Get available episodes for a specific [run]
     */
    fun getEpisodes(run: String) = history.filter { it.info.run == run }.map { it.info.episode }.distinct().sorted()


    /**
     * Get the unique list of metric names that have been captured
     */
    override val metricNames : List<String>
        get() = history.map { it.metric }.distinct().sorted()


    /**
     * Get results for a metric specified by its [name]. It will include all the runs and episodes for that metric.
     */
    override fun getMetric(name: String): List<MetricsEntry> {
        return history.filter { it.metric == name }
    }

}

internal fun Collection<MetricsEntry>.groupBy(period: ChronoUnit): Map<String, Collection<MetricsEntry>> {

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
    formatter.timeZone = TimeZone.getTimeZone(Config.defaultZoneId)
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
internal fun Collection<MetricsEntry>.getName(): String {
    return map { it.metric }.distinct().joinToString("/") { it.replace('.', ' ') }
}