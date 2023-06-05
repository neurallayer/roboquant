package org.roboquant.feeds

import org.roboquant.common.Timeframe
import org.roboquant.common.millis
import org.roboquant.common.minutes
import org.roboquant.common.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregatorFeedTest {

    @Test
    fun basic() {
        val feed = RandomWalkFeed.lastDays(1)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 15.minutes)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertTrue((items2[1].time.epochSecond - items2[0].time.epochSecond) >= (15*60))
        assertEquals(feed.timeframe, aggFeed.timeframe)

        val pb = items2.first().actions.first()
        assertTrue(pb is PriceBar)
        assertEquals(15.minutes, pb.timeSpan)

        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun basic2() {
        // 5-seconds window with 1 millis resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val feed = RandomWalkFeed(timeframe, 1.millis, generateBars = false)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 1.seconds)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertEquals(4, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        assertEquals(feed.timeframe, aggFeed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun combined() {
        // 5-seconds window with 1 millis resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val rw = RandomWalkFeed(timeframe,1.millis, generateBars = false)
        val items1 = rw.toList()

        val aggFeed1 = AggregatorFeed(rw, 1.seconds)
        assertEquals(4, aggFeed1.toList().size)
        val pb1 = aggFeed1.toList().first().actions.first()
        assertTrue(pb1 is PriceBar)
        assertEquals(1.seconds, pb1.timeSpan)

        val aggFeed2 = AggregatorFeed(rw, 2.seconds)
        assertEquals(2, aggFeed2.toList().size)
        val pb2 = aggFeed2.toList().first().actions.first()
        assertTrue(pb2 is PriceBar)
        assertEquals(2.seconds, pb2.timeSpan)

        val feed = CombinedFeed(aggFeed1, aggFeed2)
        val items2 = feed.toList()
        assertEquals(6, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        assertEquals(feed.timeframe, feed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }


}