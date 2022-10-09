package org.roboquant.metrics

import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class ScorecardMetricTest {

    @Test
    fun test() {
        val metric = ScorecardMetric("myprefix.")
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isNotEmpty())
        assertEquals(0.0, result["myprefix.winners"])
        assertEquals(0.0, result["myprefix.loosers"])
        assertNull(result["otherprefix.loosers"])
    }

}