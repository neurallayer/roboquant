package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.assertTrue

internal class PriceCorrelationChartTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears(1)
        val chart = PriceCorrelationChart(feed, feed.assets)
        chart.render()
        println(chart.asHTML())
        assertTrue(chart.asHTML().isNotBlank())
    }

}