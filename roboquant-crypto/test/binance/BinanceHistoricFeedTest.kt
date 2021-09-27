package org.roboquant.binance

import org.junit.Test
import org.roboquant.common.TimeFrame

internal class BinanceHistoricFeedTest {

    @Test
    fun test() {
        System.getProperty("TEST_BINANCE") ?: return
        val feed = BinanceHistoricFeed()
        val tf = TimeFrame.pastPeriod(100)
        feed.retrieve("BTC-BUSD", timeFrame = tf)
    }

}