package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.assertTrue

internal class PriceChartTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears(1)
        val chart = PriceChart(feed, feed.assets.first(), listOf())
        assertTrue(chart.asHTML().isNotBlank())
    }

}