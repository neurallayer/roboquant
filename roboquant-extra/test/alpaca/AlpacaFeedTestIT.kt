package org.roboquant.alpaca

import org.roboquant.common.TimeFrame
import org.roboquant.common.findBySymbol
import kotlin.test.Test
import kotlin.test.assertTrue


internal class AlpacaFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaFeed()
        val assets = feed.availableAssets
        val apple = assets.findBySymbol("AAPL")
        feed.subscribe(apple)
        feed.disconnect()
        feed.connect()
    }

    @Test
    fun testHistoric() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.isNotEmpty())
        feed.retrieve("AAPL", timeFrame = TimeFrame.pastPeriod(100))
    }

}