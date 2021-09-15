package org.roboquant.strategies

import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.junit.Test
import kotlin.test.assertTrue

internal class EMACrossoverTest {

    @Test
    fun simple() = runBlocking {
        val feed = RandomWalk.lastYears()

        val strategy = EMACrossover()

        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = SilentLogger() )
        roboquant.run( feed)
    }

    @Test
    fun simple2()  {
        val strategy1 = EMACrossover.shortTerm()
        val strategy2 = EMACrossover.longTerm()
        assertTrue(strategy1.fast < strategy2.fast)
        assertTrue(strategy1.slow < strategy2.slow)
    }

}