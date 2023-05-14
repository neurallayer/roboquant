/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.loggers

import org.roboquant.Run
import org.roboquant.Step
import org.roboquant.TestData
import org.roboquant.common.days
import org.roboquant.common.plus
import org.roboquant.metrics.metricResultsOf
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MemoryLoggerTest {

    @Test
    fun memoryLogger() {
        val logger = MemoryLogger(showProgress = false)
        assertTrue(logger.metricNames.isEmpty())


        val metrics = TestData.getMetrics()
        val runInfo = TestData.getRunInfo()
        val step = TestData.getStep()

        logger.start(runInfo)
        logger.log(metrics, step)
        logger.end(runInfo)
        assertFalse(logger.metricNames.isEmpty())
        assertEquals(metrics.size, logger.metricNames.size)

        val t = logger.getMetric(metrics.keys.first())
        assertEquals(1, t.size)

        val z = logger.getMetric(metrics.keys.first())
        assertEquals(1, z.size)


        assertEquals(1, logger.runs.size)

        assertTrue(z.summary().toString().isNotEmpty())
        assertTrue(z.min() <= z.max())

        repeat(4) {
            logger.log(metrics, TestData.getStep())
        }
    }

    @Test
    fun testMetricsEntry() {
        val logger = MemoryLogger(showProgress = false)
        logger.start(TestData.getRunInfo())
        repeat(12) {
            val metrics = metricResultsOf("key1" to it)
            logger.log(metrics, TestData.getStep())
        }
        val data = logger.getMetric("key1")
        assertEquals(12, data.size)
        val dataDiff = data.diff()
        assertEquals(11, dataDiff.size)
        assertEquals(1.0, dataDiff.first().value)

        val dataPerc = data.perc()
        assertEquals(11, dataPerc.size)
        assertEquals(10.0, dataPerc.last().value)

        val h = data.high(5)
        assertEquals(5, h.size)
        val max = data.max()
        assertEquals(max.value, h.last().value)

        val l = data.low(5)
        assertEquals(5, l.size)
        val min = data.min()
        assertEquals(min.value, l.first().value)

    }

    @Test
    fun groupBy() {
        val logger = MemoryLogger(showProgress = false)
        logger.start(TestData.getRunInfo())

        var step = TestData.getStep()
        repeat(50) {
            val metrics = metricResultsOf("key1" to it)
            logger.log(metrics, step)
            step = step.copy(time = step.time + 2.days)
        }

        val data = logger.getMetric("key1")
        assertEquals(50, data.size)

        assertEquals(50, data.groupBy(ChronoUnit.MINUTES).size)
        assertEquals(50, data.groupBy(ChronoUnit.DAYS).size)
        assertEquals(15, data.groupBy(ChronoUnit.WEEKS).size)
        assertEquals(4, data.groupBy(ChronoUnit.MONTHS).size)
        assertEquals(1, data.groupBy(ChronoUnit.YEARS).size)
        assertEquals(50, data.groupBy(ChronoUnit.YEARS).values.first().size)
    }

    @Test
    fun groupAndFlatten() {
        val logger = MemoryLogger(showProgress = false)
        val time = Instant.parse("2021-01-02T00:00:00Z")


        repeat(50) {
            val run = "run-$it"
            val runInfo = Run(run)
            logger.start(runInfo)
            val metrics = metricResultsOf("key1" to it)
            var step = Step(run, time = time + 2.days)
            logger.log(metrics,step)

            step = step.copy(time = step.time + 1.days)
            logger.log(metrics, step)
        }

        val data = logger.getMetric("key1")
        assertEquals(100, data.size)

        val map = data.group()
        assertEquals(50, map.size)
        assertEquals(data, map.flatten())
    }

}