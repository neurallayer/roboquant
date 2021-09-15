package org.roboquant.jupyter

import org.roboquant.logging.MemoryLogger
import org.junit.Test
import kotlin.test.assertTrue


internal class MetricHistogramTest {

    @Test
    fun test() {
        val logger = MemoryLogger()
        val data = logger.getMetric("test")
        val chart = MetricHistogram(data)
        assertTrue(chart.asHTML().isNotBlank())
    }

}