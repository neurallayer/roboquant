package org.roboquant.jupyter

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test

class SignalChartTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(1)
        val strat = EMAStrategy()
        val chart = SignalChart(feed, strat)
        assertDoesNotThrow {
            chart.asHTML()
        }

    }


}