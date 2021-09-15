package org.roboquant.metrics

import org.junit.Test
import kotlin.test.*
import org.roboquant.TestData

internal class ProgressMetricTest {

    @Test
    fun calc() {
        val metric = ProgressMetric()
        val (account, step) = TestData.metricInput()
        val result = metric.calc(account, step)
        assertTrue(result.isNotEmpty())
    }
}