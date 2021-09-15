package org.roboquant.strategies

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.SilentLogger
import org.junit.Test

internal class ParallelStrategyTest {


    @Test
    fun test() {
        val strategy = ParallelStrategy(EMACrossover(1,3), EMACrossover(3, 5))
        val feed = RandomWalk.lastYears()
        val roboquant = Roboquant(strategy, logger = SilentLogger() )
        roboquant.run( feed)
    }

}