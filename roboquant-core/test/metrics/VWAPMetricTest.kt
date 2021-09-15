package org.roboquant.metrics

import org.junit.Test
import kotlin.test.assertEquals

internal class VWAPMetricTest {

    @Test
    fun test() {
        val m = VWAPMetric()
        assertEquals(2, m.minSize)
    }

}