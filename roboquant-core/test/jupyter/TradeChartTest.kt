package org.roboquant.jupyter

import org.junit.Test
import kotlin.test.assertTrue

internal class TradeChartTest {

    @Test
    fun test() {
        val chart = TradeChart(listOf())
        assertTrue(chart.asHTML().isNotBlank())
    }

}