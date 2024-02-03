/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.charts

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertTrue

internal class TimeSeriesChartTest {


    @Test
    fun single() {
        val data = TestData.data
        val chart = TimeSeriesChart(data.values.first())

        assertDoesNotThrow {
            chart.renderJson()
        }

        assertTrue(chart.renderJson().isNotBlank())
    }

    @Test
    fun predefined() {
        val data = TestData.data
        val chart = TimeSeriesChart.walkForward(data)

        assertDoesNotThrow {
            chart.renderJson()
        }

        assertTrue(chart.renderJson().isNotBlank())
    }

    @Test
    fun predefined2() {
        val data = TestData.data
        val chart = TimeSeriesChart.walkForward(data)

        assertDoesNotThrow {
            chart.renderJson()
        }

        assertTrue(chart.renderJson().isNotBlank())
    }


    @Test
    fun fromMetrics() {
        val feed = RandomWalkFeed.lastYears(1)
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = MemoryLogger(false))
        rq.run(feed)
        assertDoesNotThrow {
            val chart = TimeSeriesChart.fromMetrics(rq.logger, "account.equity", "account.cash")
            chart.renderJson()
        }
    }

}
