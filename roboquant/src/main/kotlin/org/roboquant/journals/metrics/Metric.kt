/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.journals.metrics

import org.roboquant.common.Account
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal

/**
 * Alias for metric results, that is a Map with the key being the metric name and the value a number
 */
// typealias Map<String, Double> = Map<String, Double>

/**
 * Convert pairs of <String, number> to metric results. Any number will be converted to Double.
 */
fun metricResultsOf(vararg metricResults: Pair<String, Number>): Map<String, Double> {
    return metricResults.associate { Pair(it.first, it.second.toDouble()) }
}

/**
 * Metric represents a piece of information you want to capture during a run. Examples of metrics are
 * account value, profit and loss, alpha and beta.
 *
 * This is the interface that any metric will have to implement, so it can be invoked during a run. Notice that a metric
 * takes care of the calculations, but the storing of the results is normally done by a MetricsLogger.
 *
 */
interface Metric {

    /**
     * Calculate the metric given the [account] and [event] and return the results. This method will be invoked at the
     * end of each step in a run.
     */
    fun calculate(
        event: Event,
        account: Account,
        signals: List<Signal> = listOf(),
        orders: List<Order> = listOf()
    ): Map<String, Double>

    /**
     * Reset the state of the component to its initial state. The default implementation is to take no action.
     */
    fun reset() {
        // default is to do nothing
    }

}


