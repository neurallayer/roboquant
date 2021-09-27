package org.roboquant.alphavantage

import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.junit.Test

internal class AlphaVantageFeedTestIT {

    @Test
    fun alphaVantage() {
        System.getProperty("TEST_ALPHAVANTAGE") ?: return
        val strategy = EMACrossover.shortTerm()
        val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), logger = SilentLogger())

        val feed = AlphaVantageFeed("dummy", compensateTimeZone = true, generateSinglePrice = false)
        val asset = Asset("AAPL", AssetType.STOCK,"USD", "NASDAQ")
        feed.subscribe(asset)
        roboquant.run(feed)
    }


}