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
import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.months
import org.roboquant.feeds.random.RandomWalk
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HistoricFeedTest {

    @Test
    fun test() {
        val feed: HistoricFeed = RandomWalk.lastYears()
        val tfs = feed.split(1.months)
        assertEquals(12, tfs.size)
        assertEquals(10, feed.assets.size)
        assertFalse(feed.timeline.isEmpty())

        val s = feed.assets.first().symbol
        assertEquals(s, feed.find(s).symbol)
    }

    @Test
    fun testMerge() {
        val feed1: HistoricPriceFeed = RandomWalk.lastYears(nAssets = 2)
        val feed2: HistoricPriceFeed = RandomWalk.lastYears(nAssets = 3)
        feed1.merge(feed2)
        assertTrue(feed1.assets.containsAll(feed2.assets))
    }

    @Test
    fun play() {
        var past = Instant.MIN
        runBlocking {
            for (event in play(TestData.feed)) {
                assertTrue(event.time > past)
                past = event.time
            }
        }
    }


}