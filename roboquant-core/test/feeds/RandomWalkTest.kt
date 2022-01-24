/*
 * Copyright 2021 Neural Layer
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
import org.junit.Test
import kotlin.test.*
import org.roboquant.common.Timeframe
import org.roboquant.feeds.random.RandomWalk
import java.time.Instant
import java.time.Period


internal class RandomWalkTest {

    @Test
    fun randomly() = runBlocking {
        val feed = RandomWalk.lastYears(1, 20)
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
        val feed = RandomWalk.lastYears(generateBars = false)
        val event = play(feed).receive()
        assertTrue(event.actions[0] is TradePrice)

        val tl = Timeframe.fromYears(2010, 2012).toDays()
        val feed2 = RandomWalk(tl, generateBars = true)
        val item2 = play(feed2).receive()
        assertEquals(tl, feed2.timeline)
        assertTrue(item2.actions[0] is PriceBar)
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
    fun historic2() {
        val feed = RandomWalk.lastDays(5)
        val tf = feed.split(100)
        assertTrue(tf.isNotEmpty())
    }


    @Test
    fun filter() {
        val feed = RandomWalk.lastYears()
        val asset = feed.assets.first()
        val result = feed.filter<PriceBar> { it.asset == asset }
        assertEquals(feed.timeline.size, result.size)

    }
}