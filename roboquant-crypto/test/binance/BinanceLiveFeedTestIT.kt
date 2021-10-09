package org.roboquant.binance

import org.junit.Test
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BinanceLiveFeedTestIT {

    @Test
    fun testBinanceFeed() {
        System.getProperty("TEST_BINANCE") ?: return

        val feed = BinanceLiveFeed()

        val availableAssets = feed.availableAssets
        assertTrue(availableAssets.isNotEmpty())

        assertTrue(feed.assets.isEmpty())
        feed.subscribePriceBar("BTC-BUSD")
        assertFalse(feed.assets.isEmpty())

        val timeFrame = TimeFrame.nextMinutes(10)
        val prices = feed.filter<PriceBar>(timeFrame = timeFrame)
        assertTrue(prices.isNotEmpty())
        feed.disconnect()
    }

}

