package org.roboquant.iex

import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import kotlin.test.assertTrue


internal class IEXLiveTestIT {

    @Test
    fun test() {
        System.getProperty("iex") ?: return
        val token = System.getProperty("IEX_PUBLISHABLE_TOKEN") ?: System.getenv("IEX_PUBLISHABLE_TOKEN")
        val feed = IEXLive(token)
        val asset = Asset("AAPL")
        feed.subscribeQuotes(asset)
        assertTrue(asset in feed.assets)

        val strategy = EMACrossover()
        val logger = MemoryLogger()
        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = logger)
        val tf = TimeFrame.nextMinutes(3)
        roboquant.run(feed, tf)
        logger.summary()
    }

}