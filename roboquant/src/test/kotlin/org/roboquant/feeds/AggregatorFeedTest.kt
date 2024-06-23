/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.feeds

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.feeds.util.LiveTestFeed
import org.roboquant.strategies.EMACrossover
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AggregatorFeedTest {

    @Test
    fun basic() {
        val feed = RandomWalk.lastDays(1)
        val items1 = feed.toList()

        val aggFeed = AggregatorFeed(feed, 15.minutes)
        val items2 = aggFeed.toList()
        assertTrue(items2.isNotEmpty())
        assertTrue((items2[1].time.epochSecond - items2[0].time.epochSecond) >= (15 * 60))
        // assertEquals(feed.timeframe, aggFeed.timeframe)

        val pb = items2.first().items.first()
        assertTrue(pb is PriceBar)
        assertEquals(15.minutes, pb.timeSpan)

        assertTrue(items1.first().time <= items1.first().time)
        assertTrue(items1.last().time >= items1.last().time)
    }

    @Test
    fun aggregatorFeed2() {
        val tf = Timeframe.parse("2022-01-01T12:00:00", "2022-01-01T15:00:00")
        val feed = RandomWalk(tf, 1.minutes, nAssets = 1)
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
            if (lastTime != null) {
                assertTrue(t > lastTime)
            }
            lastTime = t
        }
        assertTrue(lastTime != null)

    }

    @Test
    fun basic2() {
        // 5-seconds window with 1-millisecond resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val feed = RandomWalk(timeframe, 1.millis, priceType = PriceItemType.TRADE)
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
        val feed = RandomWalk(timeframe, 1.millis, priceType = PriceItemType.TRADE)

        val aggFeed = AggregatorFeed(feed, 1.seconds)
        val jobs = ParallelJobs()
        aggFeed.timeframe.split(3.months).forEach {
            jobs.add {
                val rq = Roboquant(EMACrossover())
                rq.runAsync(aggFeed, timeframe = it)
            }
        }
    }

    @Test
    fun combined() {
        // 5-seconds window with 1-millisecond resolution
        val timeframe = Timeframe.parse("2022-01-01T00:00:00Z", "2022-01-01T00:00:05Z")
        val rw = RandomWalk(timeframe, 1.millis, priceType = PriceItemType.TRADE)
        val items1 = rw.toList()

        val aggFeed1 = AggregatorFeed(rw, 1.seconds)
        assertEquals(5, aggFeed1.toList().size)
        val pb1 = aggFeed1.toList().first().items.first()
        assertTrue(pb1 is PriceBar)
        assertEquals(1.seconds, pb1.timeSpan)

        val aggFeed2 = AggregatorFeed(rw, 2.seconds)
        assertEquals(3, aggFeed2.toList().size)
        val pb2 = aggFeed2.toList().first().items.first()
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
