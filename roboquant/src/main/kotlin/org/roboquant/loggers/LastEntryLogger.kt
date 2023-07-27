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

import org.roboquant.common.Observation
import org.roboquant.common.TimeSeries
import org.roboquant.common.Timeframe
import java.time.Instant
import java.util.*

/**
 * Create a mutable synchronized list
 */
// fun <K,V> mutableSynchronisedMapOf(): MutableMap<K,V> = Collections.synchronizedMap(mutableMapOf<K,V>())

/**
 * Stores the last value of a metric for a particular run in memory.
 * This is more memory efficient than the [MemoryLogger] if you only care about the last recorded result and not the
 * values of metrics at each step of a run.
 *
 * If you need access to the metric values at each step, use the [MemoryLogger] instead.
 *
 * @property showProgress display a progress bar, default is false
 */
class LastEntryLogger(var showProgress: Boolean = false) : MetricsLogger {

    // The key is runName
    private val history = Hashtable<String, MutableMap<String, Observation>>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: Map<String, Double>, time: Instant, run: String) {
        if (showProgress) progressBar.update(time)

        if (results.isNotEmpty()) {
            val map = history.getOrPut(run) { mutableMapOf() }
            for ((t, u) in results) {
                val value = Observation(time, u)
                map[t] = value
            }
        }
    }

    override fun start(run: String, timeframe: Timeframe) {
        history.remove(run)
        if (showProgress) progressBar.start(run, timeframe)
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
     * Get the unique list of metric names that have been captured
     */
    override val metricNames: List<String>
        get() = history.values.map { it.keys }.flatten().distinct().sorted()

    /**
     * Get results for the metric specified by its [name].
     */
    @Synchronized
    override fun getMetric(name: String): Map<String, TimeSeries> {
        val result = mutableMapOf<String, TimeSeries>()
        for (run in history.keys) {
            val ts = getMetric(name, run)
            if (ts.isNotEmpty()) result[run] = ts
        }
        return result
    }

    /**
     * Get results for the metric specified by its [name].
     */
    override fun getMetric(name: String, run: String): TimeSeries {
        val entries = history[run] ?: return TimeSeries(emptyList())
        val v = entries[name]
        val result = if (v == null) emptyList() else listOf(v)
        return TimeSeries(result)
    }

}
