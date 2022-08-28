/*
 * Copyright 2022 Neural Layer
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


import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.RunPhase
import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertTrue

internal class MetricSchedulerTest {



    @Test
    fun test() {
        val pm = ProgressMetric()
        var metric = MetricScheduler(MetricScheduler.everyFriday, pm)

        metric.start(RunPhase.MAIN)
        val (account, event) = TestData.metricInput()
        var result = metric.calculate(account, event)
        assertTrue(result.isNotEmpty())
        metric.end(RunPhase.MAIN)
        assertDoesNotThrow { metric.reset() }

        metric = MetricScheduler(MetricScheduler.lastFridayOfMonth, pm)
        result = metric.calculate(account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.lastFridayOfYear, pm)
        result = metric.calculate(account, event)
        assertTrue(result.isEmpty())

        metric = MetricScheduler(MetricScheduler.dailyAt(16), pm)
        result = metric.calculate(account, event)
        assertTrue(result.isEmpty())


    }
}