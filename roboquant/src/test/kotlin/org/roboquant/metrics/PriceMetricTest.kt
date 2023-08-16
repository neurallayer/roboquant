package org.roboquant.metrics

import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class PriceMetricTest {

    @Test
    fun basic() {
        val metric = PriceMetric("OPEN")
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertEquals(1, result.size)
        assertContains(result, "price.open.aaa")
    }

}