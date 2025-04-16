package org.roboquant.journals

import org.roboquant.common.years
import org.roboquant.run
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMACrossover
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class JournalTest {

    @Test
    fun basic() {
        val feed = RandomWalk.lastYears(5)
        val journal = BasicJournal()
        run(feed, EMACrossover(), journal=journal)
        assertTrue(journal.nEvents > 0)
        assertTrue(journal.nOrders > 0)
        assertTrue(journal.maxPositions > 0)
        assertTrue(journal.nItems > 0)
        assertTrue(journal.nSignals > 0)
        assertTrue(journal.lastTime!! > Instant.MIN)

    }

    @Test
    fun multiRun() {
        val feed = RandomWalk.lastYears(5)
        val mrj = MultiRunJournal {
            MemoryJournal(AccountMetric())
        }
        val tfs = feed.timeframe.split(1.years)
        for (tf in tfs) {
            run(feed, EMACrossover(), journal=mrj.getJournal(), timeframe=tf)
        }
        assertContains(mrj.getMetricNames(), "account.equity")
        assertEquals(tfs.size, mrj.getRuns().size)

    }
}
