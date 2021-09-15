package org.roboquant.metrics

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import kotlin.test.assertTrue

internal class AccountBetaTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears(10, 20)
        val marketAsset = feed.assets.first()
        val strategy = EMACrossover.shortTerm()
        val accountBetaMetric = AccountBeta(marketAsset,50)
        val logger = MemoryLogger(false)
        val roboquant = Roboquant(strategy, accountBetaMetric, logger = logger)
        roboquant.run(feed)
        val beta = logger.getMetric("account.beta").last().value as Double

        assertTrue(! beta.isNaN())
    }

}