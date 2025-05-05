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

package org.roboquant.metrics

import org.roboquant.TestData
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.journals.MemoryJournal
import org.roboquant.strategies.EMACrossover
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class ReturnsMetricTest {

    @Test
    fun basic() {
        val metric = ReturnsMetric()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(event, account, listOf(), listOf())
        assertTrue(result.isEmpty())
    }

    @Test
    fun basic2() {
        val metric = ReturnsMetric2(minSize = 250)
        val feed = RandomWalk.lastYears(2)
        val j = MemoryJournal(metric)
        org.roboquant.run(feed, EMACrossover(), journal = j)
        assertContains(j.getMetricNames(), "returns.sharperatio")
    }


}
