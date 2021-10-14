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


import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

internal class MetricSchedulerTest {


    private fun calc(metric: Metric, account: Account, event: Event): MetricResults {
        metric.calculate(account, event)
        return metric.getMetrics()
    }

    @Test
    fun test() {
        val pm = ProgressMetric()
        var metric = MetricScheduler(MetricScheduler.everyFriday, pm)

        val (account, event) = TestData.metricInput()
        var result = calc(metric, account, event)
        assertTrue(result.isNotEmpty())

        metric = MetricScheduler(MetricScheduler.lastFridayOfMonth, pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.lastFridayOfYear, pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.dailyAt(16), pm)
        result = calc(metric, account, event)
        assertTrue(result.isEmpty())

    }
}