package org.roboquant.ibkr

import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.TimeFrame
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.strategies.EMACrossover
import org.junit.Test

internal class IBKRFeedTestIT {

    @Test
    fun ibkrFeed() {
        System.getProperty("TEST_IBKR") ?: return

        val feed = IBKRFeed()
        val contract = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
        feed.subscribe(contract)

        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions())
        roboquant.run(feed, TimeFrame.nextMinutes(5))
    }

}