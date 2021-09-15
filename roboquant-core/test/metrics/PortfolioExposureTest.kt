package org.roboquant.metrics


import kotlin.test.*
import org.roboquant.TestData

internal class PortfolioExposureTest {

    @Test
    fun calc() {
        val metric = PortfolioExposure()
        val (account, step) = TestData.metricInput()
        val result = metric.calc(account, step)
        assertTrue(result.isNotEmpty())
    }

}