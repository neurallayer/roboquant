package org.roboquant.metrics


import kotlin.test.*
import org.roboquant.TestData

internal class OpenPositionsTest {

    @Test
    fun calc() {
        val metric = OpenPositions()
        val (account, step) = TestData.metricInput()
        val result = metric.calc(account, step)
        assertTrue(result.isEmpty())
    }
}