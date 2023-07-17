package org.roboquant.backtest

import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.common.months
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.loggers.SilentLogger
import org.roboquant.strategies.EMAStrategy

class BacktestTest {

    val feed = RandomWalkFeed.lastYears(2)
    val rq = Roboquant(EMAStrategy.PERIODS_5_15, logger = SilentLogger())

    @Test
    fun runs() {
        val bt = Backtest(feed, rq)
        bt.singleRun()

        bt.walkForward(6.months)

        bt.monteCarlo(3.months, 50)

    }


}