package org.roboquant.metrics


import kotlin.test.*
import org.roboquant.TestData

internal class PNLTest {

    @Test
    fun basic() {
        val metric = PNL()
        val (account, step) = TestData.metricInput()
        val result = metric.calc(account, step)
        assertEquals(0.0, result.values.first())
    }

}