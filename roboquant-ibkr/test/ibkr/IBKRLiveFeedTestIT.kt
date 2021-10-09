package org.roboquant.ibkr

import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IBKRLiveFeedTestIT {

    @Test
    fun ibkrFeed() {
        System.getProperty("TEST_IBKR") ?: return

        val feed = IBKRLiveFeed()
        val contract = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
        feed.subscribe(contract)

        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
    }

}