package org.roboquant.jupyter

import org.junit.Test
import kotlin.test.assertTrue

internal class OrderChartTest {

    @Test
    fun test() {
        val chart = OrderChart(listOf())
        assertTrue(chart.asHTML().isNotBlank())
    }

}