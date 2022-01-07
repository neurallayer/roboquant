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


import kotlin.test.*
import org.roboquant.RunPhase
import org.roboquant.TestData
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class MemoryLoggerTest {

    @Test
    fun memoryLogger() {
        val logger = MemoryLogger()
        assertTrue(logger.metricNames.isEmpty())
        val metrics = TestData.getMetrics()
        logger.log(metrics, TestData.getRunInfo())
        logger.end(RunPhase.VALIDATE)
        assertFalse(logger.metricNames.isEmpty())
        assertEquals(metrics.size, logger.metricNames.size)

        val t = logger.getMetric(metrics.keys.first())
        assertEquals(1, t.size)

        val z = logger.getMetric(metrics.keys.first())
        assertEquals(1, z.size)

        assertEquals(1, logger.getEpisodes("1").size)

        assertEquals(1, logger.getRuns().size)

        assertTrue(z.summary().toString().isNotEmpty())
        assertTrue(z.min() <= z.max())

        assertTrue(logger.history.isNotEmpty())
        logger.summary(3)
    }

    @Test
    fun testMetricsEntry() {
        val logger = MemoryLogger()
        repeat(12) {
            val metrics =  mapOf("key1" to it)
            logger.log(metrics, TestData.getRunInfo())
        }
        val data = logger.getMetric("key1")
        assertEquals(12, data.size)
        val dataDiff = data.diff()
        assertEquals(11, dataDiff.size)
        assertEquals(1.0, dataDiff.first().value)

        val dataPerc = data.perc()
        assertEquals(11, dataPerc.size)
        assertEquals(0.1, dataPerc.last().value)
    }

}