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
import org.roboquant.RunPhase
import org.roboquant.metrics.MetricResults

/**
 * Stores the last value of a metric for a particular run and phase in memory. This is more memory efficient if you
 * only care about the last result and not the values of metrics at each step of a run.
 *
 * If you need access to the metric values at each step, use the [MemoryLogger] instead.
 */
class LastEntryLogger(var showProgress: Boolean = false) : MetricsLogger {

    // Key is runName + phase + metricName
    private val history = mutableMapOf<Triple<String, RunPhase, String>, MetricsEntry>()
    private val progressBar = ProgressBar()

    @Synchronized
    override fun log(results: MetricResults, info: RunInfo) {
        if (showProgress) progressBar.update(info)

        for ((t, u) in results) {
            val key = Triple(info.run, info.phase, t)
            val value = MetricsEntry(t, u.toDouble(), info)
            history[key] = value
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
     * Get the unique list of metric names that have been captured
     */
    override val metricNames : List<String>
        get() = history.map { it.key.third }.distinct().sorted()


    /**
     * Get results for the metric specified by its [name].
     */
    override fun getMetric(name: String): List<MetricsEntry> {
        return history.values.filter { it.metric == name }
    }

}
