package org.roboquant.policies

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.strategies.NoSignalStrategy
import kotlin.test.*

class BettingAgainstAccountBetaTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears()
        val assets = feed.assets.toList()
        val marketAsset = assets.first()

        val policy = BettingAgainstBeta(assets, marketAsset)
        val logger = MemoryLogger(false)
        val exp = Roboquant(NoSignalStrategy(), policy = policy, logger = logger)
        exp.run(feed)
        logger.summary(1).print()
        assertTrue(exp.broker.account.orders.closed.isNotEmpty())
    }

}