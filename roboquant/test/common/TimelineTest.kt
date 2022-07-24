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

package org.roboquant.common

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.timeseries
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TimelineTest {


    @Test
    fun beforeAfter() {
        val timeline = Timeframe.fromYears(1995, 2000).toTimeline(1.days)
        val first = timeline.first()
        val last = timeline.last()
        assertEquals(timeline.lastIndex, timeline.latestNotAfter(last))
        assertEquals(0, timeline.earliestNotBefore(first))
    }

    @Test
    fun split() {
        val tl = Timeframe.fromYears(1987, 1999).toTimeline(1.days)
        val list = tl.split(200)
        assertEquals(tl.first(), list.first().start)
        assertTrue(tl.last() < list.last().end)
    }

    @Test
    fun timeframe() {
        val timeline = Timeframe.fromYears(1987, 1999).toTimeline(1.days)
        val tf = timeline.timeframe
        assertEquals(Timeframe.inclusive(timeline.first(), timeline.last()), tf)
    }

    @Test
    fun correlation() {
        val feed = TestData.feed()
        val data = feed.filter<PriceAction>()
        val timeseries = data.timeseries()
        val corr = timeseries.correlation()
        assertTrue(corr.isNotEmpty())
        val pair = Pair(feed.assets.first(), feed.assets.first())
        assertTrue(corr[pair]!! in 0.999..1.001)
    }


}