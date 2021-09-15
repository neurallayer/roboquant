package org.roboquant.jupyter

import org.roboquant.logging.MemoryLogger
import kotlin.test.assertTrue
import org.junit.Test

internal class MetricChartTest {

    @Test
    fun test() {
        val logger = MemoryLogger()
        val data = logger.getMetric("test")
        val chart = MetricChart(data)
        assertTrue(chart.asHTML().isNotBlank())
    }

}