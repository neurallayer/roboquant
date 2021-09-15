package org.roboquant.logging


import kotlin.test.*
import org.roboquant.Phase
import org.roboquant.TestData
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class MemoryLoggerTest {

    @Test
    fun memoryLogger() {
        val logger = MemoryLogger()
        assertTrue(logger.getMetricNames().isEmpty())
        val metrics = TestData.getMetrics()
        logger.log(metrics, TestData.getRunInfo())
        logger.end(Phase.VALIDATE)
        assertFalse(logger.getMetricNames().isEmpty())
        assertEquals(metrics.size, logger.getMetricNames().size)

        val t = logger.getMetric(metrics.keys.first())
        assertEquals(1, t.size)

        val z = logger.getMetric(metrics.keys.first())
        assertEquals(1, z.size)

        assertEquals(1, logger.getEpisodes(1).size)

        assertEquals(1, logger.getRuns().size)

        assertTrue(logger.history.isNotEmpty())
        logger.summary(3)
    }

}