package org.roboquant.ta

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class TAMetricTest {

    @Test
    fun test() {
        val metric = TALibMetric("ema50",50) { series ->
            ema(series.close, 50)
        }
        assertTrue(metric.getMetrics().isEmpty())
    }
}