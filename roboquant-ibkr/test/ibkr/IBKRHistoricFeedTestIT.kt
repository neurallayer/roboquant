package org.roboquant.ibkr

import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IBKRHistoricFeedTestIT {

    @Test
    fun ibkrFeed() {
        System.getProperty("TEST_IBKR") ?: return

        val feed = IBKRHistoricFeed()
        val template = Asset("", AssetType.STOCK, "EUR", "AEB")
        feed.retrieve(template.copy(symbol = "ABN"), template.copy(symbol = "ASML"), template.copy(symbol = "KPN"))
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())

    }

}