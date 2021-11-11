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

package org.roboquant.strategies

import org.roboquant.RunPhase
import org.roboquant.metrics.MetricResults

/**
 * Recording strategy allows subclasses to record metrics during their processing.
 * With the recording flag you can enable or disable the actual recording of the metrics.
 *
 * @property prefix Prefix to use when logging a metric, default is "strategy."
 * @property recording Should metrics be recorded or not. Some strategies can record a lot of metrics, so this disables that recording
 * and as a consequence might be faster and/or use less memory.
 *
 */
abstract class RecordingStrategy(private val prefix: String = "strategy.", var recording: Boolean = false) : Strategy {


    private val metrics = mutableMapOf<String, Number>()

    /**
     * Get the recorded metrics. After this method has been invoked, the metrics are also cleared, so calling this method
     * twice in a row won't return the same result.
     *
     * @return
     */
    override fun getMetrics(): MetricResults {
        val result = metrics.toMap()
        metrics.clear()
        return result
    }

    /**
     * Record a new metric. If there is already a metric recorded with the same key, it will be overridden.
     *
     * @param key
     * @param value
     */
    protected fun record(key: String, value: Number) {
        if (!recording) return
        metrics["$prefix$key"] = value
    }


    /**
     * At the start of a phase, any recorded metrics will be cleared.
     */
    override fun start(runPhase: RunPhase) {
        metrics.clear()
    }
}