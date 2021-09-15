package org.roboquant.metrics


import org.junit.Test
import org.roboquant.TestData
import kotlin.test.assertTrue

internal class SharpRatioTest {

    @Test
    fun basic() {
        val metric = SharpRatio()
        val (account, step) = TestData.metricInput()
        val result = metric.calc(account, step)
        assertTrue(result.isEmpty())
    }

}