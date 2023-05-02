/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.feeds.util.play
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class RandomWalkFeedTest {

    @Test
    fun randomly() = runBlocking {
        val feed = RandomWalkFeed.lastYears(1, 20)
        var cnt = 0
        var now = Instant.MIN
        for (step in play(feed)) {
            assertTrue(step.time >= now)
            now = step.time
            cnt++
        }
        assertEquals(feed.timeline.size, cnt)
        assertEquals(feed.assets.size, 20)
    }

    @Test
    fun itemTypes() = runBlocking {
        val feed = RandomWalkFeed.lastYears(generateBars = false)
        val event = play(feed).receive()
        assertTrue(event.actions[0] is TradePrice)

        val tl = Timeframe.fromYears(2010, 2012).toTimeline(1.days)
        val feed2 = RandomWalkFeed(tl, generateBars = true)
        val item2 = play(feed2).receive()
        assertEquals(tl, feed2.timeline)
        assertTrue(item2.actions[0] is PriceBar)
    }

    @Test
    fun historic() {
        val feed = RandomWalkFeed.lastYears()
        val tf = feed.split(100)
        assertTrue(tf.isNotEmpty())

        val tf2 = feed.split(50.days)
        assertTrue(tf2.isNotEmpty())

        val tf3 = feed.sample(10).first()
        assertFalse(tf3.isSingleDay(ZoneId.of("UTC")))
    }

    @Test
    fun historic2() {
        val feed = RandomWalkFeed.lastDays(5)
        val tf = feed.split(100)
        assertTrue(tf.isNotEmpty())
    }


    @Test
    fun reproducable() {
        val timeline = Timeframe.fromYears(2000, 2001).toTimeline(1.days)
        val feed1 = RandomWalkFeed(timeline, seed = 10)
        val feed2 = RandomWalkFeed(timeline, seed = 10)
        val feed3 = RandomWalkFeed(timeline, seed = 11)

        assertEquals(feed1.assets, feed2.assets)
        assertEquals(feed1.assets, feed3.assets)

        val result1 = feed1.filter<PriceBar> { it.asset.symbol == "ASSET1" }
        val result2 = feed2.filter<PriceBar> { it.asset.symbol == "ASSET1" }
        val result3 = feed3.filter<PriceBar> { it.asset.symbol == "ASSET1" }

        assertEquals(result1.toString(), result2.toString())
        assertNotEquals(result1.toString(), result3.toString())
    }

    @Test
    fun filter() {
        val feed = RandomWalkFeed.lastYears()
        val asset = feed.assets.first()
        val result = feed.filter<PriceBar> { it.asset == asset }
        assertEquals(feed.timeline.size, result.size)
    }

    @Test
    fun validate() {
        val feed = RandomWalkFeed.lastYears()
        val errors = feed.validate()
        assertTrue(errors.isEmpty())

        val errors2 = feed.validate(maxDiff = 0.000001)
        assertFalse(errors2.isEmpty())
    }
}