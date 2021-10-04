package org.roboquant.binance

import org.junit.Test
import org.roboquant.common.TimeFrame
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BinanceHistoricFeedTest {

    @Test
    fun test() {
        System.getProperty("TEST_BINANCE") ?: return
        val feed = BinanceHistoricFeed()
        val tf = TimeFrame.pastPeriod(100)
        feed.retrieve("BTC-BUSD", timeFrame = tf)
        assertEquals(1, feed.assets.size)
        assertTrue(feed.availableAssets.isNotEmpty())
    }

}