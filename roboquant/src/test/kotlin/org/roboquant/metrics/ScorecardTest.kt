package org.roboquant.metrics

import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class ScorecardTest {

    @Test
    fun test() {
        val metric = Scorecard()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isNotEmpty())
        assertEquals(0.0, result["winners"])
        assertEquals(0.0, result["loosers"])
    }

}