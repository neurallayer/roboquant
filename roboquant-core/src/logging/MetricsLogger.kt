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
import org.roboquant.common.Component
import org.roboquant.metrics.MetricResults

/**
 * Interface that a metrics logger will need to implement. It is called by roboquant after metrics have been calculated
 * to store and/or log them.
 *
 * A metrics logger also extends the [Component] interface. This interface exposes [Component.getMetrics], hwowever this
 * method should return metrics about the Component and not the logged metrics.
 */
interface MetricsLogger : Component {

    /**
     * Log the [results of metrics]. It should be noted that the provided results can be empty. Also [info] is provided
     * about when these results where captured.
     */
    fun log(results: MetricResults, info: RunInfo)

    /**
     * Get all the logged entries for a specific metric. This is optional to implement since not all loggers store
     * the metrics. Use [metricNames] to see which metrics are available.
     */
    fun getMetric(name: String) : List<MetricsEntry> = listOf()

    /**
     * The metrics that are available and can be retreived with the [getMetric].
     */
    val metricNames : List<String>
        get() = listOf()
}


