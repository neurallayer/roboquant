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

import org.roboquant.common.TimeSeries
import org.roboquant.common.Timeframe
import java.time.Instant

/**
 * Store metric results in memory. Very convenient in a Jupyter notebook when you want to inspect or visualize
 *  metric results after a run. This is also the default metric logger for roboquant if none is specified.
 *
 * If you log very large amounts of metric data, this could cause memory issues. But for normal back testing,
 * this should not pose a problem.
 *
 * By default, a bar will be shown that shows the progress of a run, but by setting [showProgress] to false this can be
 * disabled. And although the MemoryLogger can be used in multiple runs at the same time, that is not true for the
 * progressbar.
 *
 * @property showProgress should a progress bar be displayed, default is true
 */
class MemoryLogger(var showProgress: Boolean = true) : MetricsLogger {

    internal class Entry(val time: Instant, val metrics: Map<String, Double>)

    internal val history = mutableMapOf<String, MutableList<Entry>>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: Map<String, Double>, time: Instant, run: String) {
        if (showProgress) progressBar.update(time)
        if (results.isEmpty()) return
        val entries = history.getOrPut(run) { mutableListOf() }
        entries.add(Entry(time, results))
    }

    override fun start(run: String, timeframe: Timeframe) {
        if (showProgress) {
            progressBar.start(run, timeframe)
        }
        // Clear any previous run with the same name
        history.remove(run)
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
    override fun getMetric(name: String): Map<String, TimeSeries> {
        val result = mutableMapOf<String, TimeSeries>()
        for ((run, entries) in history) {
            val values = mutableListOf<Double>()
            val times = mutableListOf<Instant>()
            entries.forEach {
                val e = it.metrics[name]
                if (e != null) {
                    values.add(e)
                    times.add(it.time)
                }
            }
            if (values.isNotEmpty()) result[run] = TimeSeries(times, values.toDoubleArray())
        }
        return result
    }

}

