package org.roboquant.questdb

import org.junit.jupiter.api.io.TempDir
import org.roboquant.Roboquant
import org.roboquant.common.Timeframe
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuestDBMetricsLoggerTest {

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
        val logger =  QuestDBMetricsLogger(folder.toPath())
        val feed = RandomWalkFeed.lastYears(1)
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = logger)
        rq.run(feed, name="myrun")
        val equity = logger.getMetric("account.equity", "myrun")
        assertTrue(equity.isNotEmpty())

        logger.start("myrun", Timeframe.INFINITE)
        logger.log(mapOf("aaa" to 12.0), Instant.now(), "myrun")
        val equity2 = logger.getMetric("account.equity", "myrun")
        assertFalse(equity2.isNotEmpty())

        val aaa = logger.getMetric("aaa", "myrun")
        assertEquals(1, aaa.size)

        feed.close()

        logger.close()

        val logger2 =  QuestDBMetricsLogger(folder.toPath())
        logger2.loadPreviousRuns()
        val aaa2 = logger2.getMetric("aaa", "myrun")
        assertEquals(1, aaa2.size)
    }

}