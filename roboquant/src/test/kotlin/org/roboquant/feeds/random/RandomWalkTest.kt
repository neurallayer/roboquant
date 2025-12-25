/*
 * Copyright 2020-2026 Neural Layer
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

import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.common.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RandomWalkTest {


    @Test
    fun historic() {
        val feed = RandomWalk.lastYears()
        val tf2 = feed.timeframe.split(50.days)
        assertTrue(tf2.isNotEmpty())
    }

    @Test
    fun toList() {
        val feed = RandomWalk.lastYears()
        val list = feed.toList()
        assertTrue(list.isNotEmpty())
        assertEquals(list.size, feed.toList().size)
    }

    @Test
    fun reproducable() {
        val timeline = Timeframe.fromYears(2000, 2001)
        val feed = RandomWalk(timeline, seed = 10)

        val symbol = feed.assets.first().symbol
        val result1 = feed.filter<PriceBar> { it.asset.symbol == symbol }
        val result2 = feed.filter<PriceBar> { it.asset.symbol == symbol }

        assertTrue(result1.isNotEmpty())
        assertEquals(result1.size, result2.size)

        assertEquals(result1.toString(), result2.toString())
    }

    @Test
    fun filter() {
        val feed = RandomWalk.lastYears()
        val asset = feed.assets.first()
        val result = feed.filter<PriceBar> { it.asset == asset }
        assertEquals(feed.timeline.size, result.size)
    }


}
