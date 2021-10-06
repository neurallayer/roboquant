package org.roboquant.iex

import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IEXLiveTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_IEX") ?: return
        val token = System.getProperty("IEX_PUBLISHABLE_TOKEN") ?: System.getenv("IEX_PUBLISHABLE_TOKEN")
        val feed = IEXLive(token)
        val asset = Asset("AAPL")
        feed.subscribeQuotes(asset)
        assertTrue(asset in feed.assets)

        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
    }

}