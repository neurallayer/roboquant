package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.*
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.random.RandomWalk
import java.time.Instant
import java.time.Period


internal class RandomWalkTest {

    @Test
    fun randomly() = runBlocking{
        val feed = RandomWalk.lastYears(1, 20)
        var cnt = 0
        var now = Instant.MIN
        for (step in play(feed)) {
            assertTrue( step.now >= now )
            now = step.now
            cnt++
        }
        assertEquals(feed.timeline.size, cnt)
        assertEquals(feed.assets.size, 20)
    }

    @Test
    fun itemTypes() = runBlocking{
        val feed = RandomWalk.lastYears(generateBars = false)
        val event = play(feed).receive()
        assertTrue(event.actions[0] is TradePrice)

        val tl = TimeFrame.fromYears(2010, 2012).toDays()
        val feed2 = RandomWalk(tl, generateBars = true)
        val item2 = play(feed2).receive()
        assertTrue( item2.actions[0] is PriceBar)
    }


    @Test
    fun historic() {
        val feed = RandomWalk.lastYears()
        val tf = feed.split(100)
        assertTrue(tf.isNotEmpty())

        val tf2 = feed.split(Period.ofDays(50))
        assertTrue(tf2.isNotEmpty())

        val tf3 = feed.sample(10)
        assertFalse(tf3.isSingleDay())
    }


    @Test
    fun filter() {
        val feed = RandomWalk.lastYears()
        val asset = feed.assets.first()
        val result = feed.filter<PriceBar> { it.asset == asset }
        assertEquals(feed.timeline.size, result.size)

    }
}