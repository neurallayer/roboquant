package org.roboquant.strategies.utils

import org.junit.Test
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceBarBufferTest {

    @Test
    fun test() {
        val pb = PriceBarBuffer(20)
        assertFalse(pb.isAvailable())
        val feed = RandomWalk.lastYears()
        val asset = feed.assets.first()
        val data = feed.filter<PriceBar> { it.asset === asset }
        for (entry in data) {
            pb.update(entry.second, entry.first)
        }
        assertTrue(pb.isAvailable())
        assertTrue(pb.typical.isNotEmpty())
    }

}