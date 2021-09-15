package org.roboquant.metrics


import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import kotlin.test.assertTrue

internal class PriceMetricTest {

    @Test
    fun basic() {

        val (account, step) = TestData.metricInput()
        val metric = PriceMetric(Asset("Dummy"))

        metric.calculate(account, step)
        assertTrue(metric.getMetrics().isEmpty())
    }

}