package org.roboquant.metrics

import org.roboquant.TestData
import kotlin.test.*


internal class ScorecardMetricTest {

    @Test
    fun test() {
        val metric = ScorecardMetric()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isNotEmpty())
        assertEquals(0.0, result["scorecard.winners"])
        assertEquals(0.0, result["scorecard.loosers"])
        assertNull(result["otherprefix.loosers"])

        metric.reset()
        val result2 = metric.calculate(account, event)
        assertEquals(result, result2)
    }

}