package org.roboquant.feeds.iex

import org.roboquant.common.Config
import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import kotlin.test.assertTrue

internal class IEXFeedTestIT {

    @Test
    fun test() {
        System.getProperty("IEX_TEST") ?: return
        val token = Config.getProperty("IEX_TOKEN")!!
        val feed = IEXFeed(token)
        val asset = Asset("AAPL")
        feed.retrieveIntraday(asset)
        assertTrue(asset in feed.assets)

        val strategy = EMACrossover()
        val logger = MemoryLogger()
        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = logger)
        roboquant.run(feed)
        logger.summary(10)
    }

    @Test
    fun testPriceBar() {
        System.getProperty("IEX_TEST") ?: return
        val token = Config.getProperty("IEX_TOKEN")!!
        val feed = IEXFeed(token)
        val asset = Asset("AAPL")
        feed.retrievePriceBar(asset)
        Thread.sleep(2000)
        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
        roboquant.run(feed)
        roboquant.broker.account.summary().log()
        roboquant.logger.summary().log()

    }


}