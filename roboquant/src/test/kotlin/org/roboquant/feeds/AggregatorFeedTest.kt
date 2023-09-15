package org.roboquant.feeds

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.LiveTestFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.strategies.EMAStrategy
import java.time.Instant
import java.time.temporal.ChronoUnit
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
        assertTrue((items2[1].time.epochSecond - items2[0].time.epochSecond) >= (15 * 60))
        // assertEquals(feed.timeframe, aggFeed.timeframe)

        val pb = items2.first().actions.first()
        assertTrue(pb is PriceBar)
        assertEquals(15.minutes, pb.timeSpan)

        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun aggregatorFeed2() {
        val tf = Timeframe.parse("2022-01-01T12:00:00", "2022-01-01T15:00:00")
        val feed = RandomWalkFeed(tf, 1.minutes, nAssets = 1)
        val ts = 15.minutes
        val aggFeed = AggregatorFeed(feed, ts)
        var lastTime: Instant? = null
        aggFeed.apply<PriceBar> { pb, t ->
            assertEquals(ts, pb.timeSpan)
            if (lastTime != null) {
                val diff = lastTime!!.until(t, ChronoUnit.MILLIS)
                assertEquals(15 * 60 * 1000L, diff)
            }
            lastTime = t
        }
        assertTrue(lastTime != null)

    }

    @Test
    fun aggregatorLiveFeed() {
        val delay = 5
        val feed = LiveTestFeed(50..100, delayInMillis = delay)
        val ts = (delay * 10).millis
        val aggFeed = AggregatorLiveFeed(feed, ts)
        var lastTime: Instant? = null
        aggFeed.apply<PriceBar> { pb, t ->
            assertEquals(ts, pb.timeSpan)
            // println("$t ${Instant.now()}")
            if (lastTime != null) {
                val diff = lastTime!!.until(t, ChronoUnit.MILLIS)
                assertEquals(delay * 10L, diff)
            }
            lastTime = t
        }
        assertTrue(lastTime != null)

    }

    @Test
    fun basic2() {
        // 5-seconds window with 1-millisecond resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val feed = RandomWalkFeed(timeframe, 1.millis, generateBars = false)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 1.seconds)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertEquals(5, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        // assertEquals(feed.timeframe, aggFeed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun parallel() {
        // 5-seconds window with 1-millisecond resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val feed = RandomWalkFeed(timeframe, 1.millis, generateBars = false)

        val aggFeed = AggregatorFeed(feed, 1.seconds)
        val jobs = ParallelJobs()
        aggFeed.timeframe.split(3.months).forEach {
            jobs.add {
                val rq = Roboquant(EMAStrategy(), logger = LastEntryLogger())
                rq.runAsync(aggFeed, it)
            }
        }
    }

    @Test
    fun combined() {
        // 5-seconds window with 1-millisecond resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val rw = RandomWalkFeed(timeframe, 1.millis, generateBars = false)
        val items1 = rw.toList()

        val aggFeed1 = AggregatorFeed(rw, 1.seconds)
        assertEquals(5, aggFeed1.toList().size)
        val pb1 = aggFeed1.toList().first().actions.first()
        assertTrue(pb1 is PriceBar)
        assertEquals(1.seconds, pb1.timeSpan)

        val aggFeed2 = AggregatorFeed(rw, 2.seconds)
        assertEquals(3, aggFeed2.toList().size)
        val pb2 = aggFeed2.toList().first().actions.first()
        assertTrue(pb2 is PriceBar)
        assertEquals(2.seconds, pb2.timeSpan)

        val feed = CombinedFeed(aggFeed1, aggFeed2)
        val items2 = feed.toList()
        assertEquals(8, items2.size)

        // at least 1 second between event times
        assertTrue((items2[1].time.toEpochMilli() - items2[0].time.toEpochMilli()) >= 1000)

        assertEquals(feed.timeframe, feed.timeframe)
        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }


}
