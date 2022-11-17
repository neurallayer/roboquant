/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.common

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.timeseries
import org.roboquant.feeds.toDoubleArray
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TimelineTest {

    private val timeline = Timeframe.fromYears(1995, 2000).toTimeline(1.days)

    @Test
    fun beforeAfter() {
        val first = timeline.first()
        val last = timeline.last()
        assertEquals(timeline.lastIndex, timeline.latestNotAfter(last))
        assertEquals(0, timeline.earliestNotBefore(first))
    }


    @Test
    fun sample() {
        val timeframes = timeline.sample(100, 10)
        assertEquals(10, timeframes.size)
    }

    @Test
    fun split() {
        val list = timeline.split(200)
        assertEquals(timeline.first(), list.first().start)
        assertEquals(timeline.last(), list.last().end)
    }

    @Test
    fun timeframe() {
        val tf = timeline.timeframe
        assertEquals(Timeframe(timeline.first(), timeline.last(), true), tf)
    }

    @Test
    fun correlation() {
        val feed = TestData.feed()
        val data = feed.filter<PriceAction>()
        val timeseries = data.timeseries()

        val (asset, first) = timeseries.entries.first()
        assertEquals(data.lastIndex, first.toDoubleArray().lastIndex)
        assertEquals(data.filter { it.second.asset == asset }.map{ it.first }, first.timeline())

        val corr = timeseries.correlation(excludeSame = false)
        assertTrue(corr.isNotEmpty())
        val pair = Pair(feed.assets.first(), feed.assets.first())
        assertTrue(corr[pair]!! in 0.999..1.001)

        val arr = data.map { it.second }.toDoubleArray()
        assertTrue { arr.isNotEmpty() }
    }

    @Test
    fun doubleArray() {
        val feed = TestData.feed()
        val data = feed.filter<PriceAction>()

        val arr = data.map { it.second }.toDoubleArray()
        assertTrue { arr.isNotEmpty() }
    }

}