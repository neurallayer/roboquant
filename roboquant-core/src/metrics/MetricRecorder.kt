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

package org.roboquant.metrics


/**
 * Recording strategy allows subclasses to record metrics during their processing.
 * With the recording flag you can enable or disable the actual recording of the metrics.
 *
 * @property prefix
 */
class MetricRecorder(private val prefix: String = "", var enabled: Boolean = false) {


    private var metrics = mutableMapOf<String, Number>()

    /**
     * Get the recorded metrics. After this method has been invoked, the metrics are also cleared, so calling this method
     * twice in a row won't return the same result.
     *
     * @return
     */
    fun getMetrics(): MetricResults {
        val result = metrics
        metrics = mutableMapOf()
        return result
    }

    /**
     * Record new metrics. If there is already a metric recorded with the same key, it will be overridden.
     *
     * @param metrics
     */
    fun record(vararg metrics: Pair<String, Number>) {
        if (!enabled) return
        metrics.forEach {
            this.metrics["$prefix${it.first}"] = it.second
        }
    }

    /**
     * Record a new metric. If there is already a metric recorded with the same key, it will be overwritten.
     *
     * @param key
     * @param value
     */
    fun record(key: String, value: Number) {
        if (!enabled) return
        metrics["$prefix$key"] = value
    }

    /**
     * Clear any metrics hold
     */
    fun clear() = metrics.clear()

}