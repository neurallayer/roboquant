package org.roboquant.alpaca

import org.roboquant.common.TimeFrame
import org.roboquant.common.getBySymbol
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertTrue


internal class AlpacaLiveFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        val assets = feed.availableAssets
        val apple = assets.getBySymbol("AAPL")
        feed.subscribe(apple)
        feed.disconnect()
        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
        feed.connect()
        feed.disconnect()
    }

    @Test
    fun testHistoric() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.isNotEmpty())
        feed.retrieve("AAPL", timeFrame = TimeFrame.pastPeriod(100))
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())
    }

}