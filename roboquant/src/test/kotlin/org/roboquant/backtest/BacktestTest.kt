package org.roboquant.backtest

import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.common.months
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BacktestTest {

    private val feed = RandomWalkFeed.lastYears(2)

    @Test
    fun single() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.singleRun()
        val data = rq.logger.getMetric("account.equity")
        assertEquals(1, data.size)
    }

    @Test
    fun wf() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.walkForward(6.months)
        val data = rq.logger.getMetric("account.equity")
        assertTrue(data.size >= 3)
    }

    @Test
    fun mc() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.monteCarlo(3.months, 50)
        val data = rq.logger.getMetric("account.equity")
        assertEquals(50, data.size)
    }


}