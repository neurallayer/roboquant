package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.logging.MemoryLogger
import kotlin.test.*

internal class MetricBoxChartTest {

    @Test
    fun test() {
        val logger = MemoryLogger()
        val data = logger.getMetric("test")
        val chart = MetricBoxChart(data)
        assertTrue(chart.asHTML().isNotBlank())
    }

}