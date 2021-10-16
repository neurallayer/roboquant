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

import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * This is the abstract base class for many type of metrics that can calculate a metric fast and immediately return
 * the results. The only method required to implement is [calc]
 *
 * More advanced type of metrics might prefer to implement the [Metric] interface directly.
 */
abstract class SimpleMetric : Metric {

    private var values: MetricResults = mapOf()

    /**
     * You only need to implement [calc]
     *
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event) {
        values = calc(account, event)
    }

    /**
     * Based on the provided [account] and [event], calculate any metrics and return them directly. This method needs to
     * be implemented in sub-classes.
     */
    abstract fun calc(account: Account, event: Event) : MetricResults

    override fun getMetrics(): MetricResults {
        val result = values
        values = emptyMap()
        return result
    }

    override fun start(runPhase: RunPhase) {
        values = emptyMap()
    }

    override fun reset() {
        values = emptyMap()
    }
}