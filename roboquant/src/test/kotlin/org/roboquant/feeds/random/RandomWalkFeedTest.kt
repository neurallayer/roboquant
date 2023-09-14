/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.feeds.random

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.feeds.*
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.feeds.util.play
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        assertEquals(feed.toList().size, cnt)
        assertEquals(feed.assets.size, 20)
    }

    @Test
    fun itemTypes() = runBlocking {
        val feed = RandomWalkFeed.lastYears(generateBars = false)
        val event = play(feed).receive()
        assertTrue(event.actions.first() is TradePrice)

        val tl = Timeframe.fromYears(2010, 2012)
        val feed2 = RandomWalkFeed(tl, generateBars = true)
        val item2 = play(feed2).receive()
        assertTrue(item2.actions.first() is PriceBar)
    }

    @Test
    fun historic() {
        val feed = RandomWalkFeed.lastYears()
        val tf2 = feed.timeframe.split(50.days)
        assertTrue(tf2.isNotEmpty())
    }

    @Test
    fun toList() {
        val feed = RandomWalkFeed.lastYears()
        val list = feed.toList()
        assertTrue(list.isNotEmpty())
        assertEquals(list.size, feed.toList().size)
    }

    @Test
    fun reproducable() {
        val timeline = Timeframe.fromYears(2000, 2001)
        val feed = RandomWalkFeed(timeline, seed = 10)

        val symbol = feed.assets.first().symbol
        val result1 = feed.filter<PriceBar> { it.asset.symbol == symbol }
        val result2 = feed.filter<PriceBar> { it.asset.symbol == symbol }

        assertTrue(result1.isNotEmpty())
        assertEquals(result1.size, result2.size)

        assertEquals(result1.toString(), result2.toString())
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
        val feed = HistoricTestFeed(90..110)

        // within 2% range
        val errors = feed.validate(maxDiff = 0.02)
        assertTrue(errors.isEmpty())

        // within 0.5% range
        val errors2 = feed.validate(maxDiff = 0.005)
        assertFalse(errors2.isEmpty())
    }
}
