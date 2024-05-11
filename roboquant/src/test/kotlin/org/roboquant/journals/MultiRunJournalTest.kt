package org.roboquant.journals

import org.roboquant.common.years
import org.roboquant.run
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals


internal class MultiRunJournalTest {

    @Test
    fun basic() {
        val feed = RandomWalkFeed.lastYears(5)
        val mrj = MultiRunJournal {
            MemoryJournal(AccountMetric())
        }
        val tfs = feed.timeframe.split(1.years)
        for (tf in tfs) {
            run(feed, EMAStrategy(), mrj.getJournal(tf.toString()), timeframe=tf)
        }
        assertContains(mrj.getMetricNames(), "account.equity")
        assertEquals(tfs.size, mrj.getRuns().size)

    }
}
