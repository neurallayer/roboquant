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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FeedTest {

    class MyFeed : Feed {
        override suspend fun play(channel: EventChannel) {
            // Intentional empty
        }
    }

    class MyFeed2 : AssetFeed {
        override val assets: SortedSet<Asset>
            get() = sortedSetOf(Asset("AAA"), Asset("AAB"))

        override suspend fun play(channel: EventChannel) {
            // Intentional empty
        }
    }

    @Test
    fun basic() {
        val feed = MyFeed()
        assertEquals(Timeframe.INFINITE, feed.timeframe)
    }

    @Test
    fun basic2() {
        val feed = MyFeed2()
        assertEquals(Timeframe.INFINITE, feed.timeframe)
        assertContains(feed.assets, Asset("AAB"))
        assertEquals(Asset("AAA"), feed.assets.first())
        assertEquals(Asset("AAB"), feed.assets.last())
    }

    @Test
    fun filter() {
        val feed = TestData.feed()
        assertDoesNotThrow {
            feed.filter<PriceAction>(timeframe = Timeframe.fromYears(1901, 2000)).filter {
                it.second.getPrice() > 0.0
            }
        }
    }

    @Test
    fun toList() {
        val feed = TestData.feed()
        val l = feed.toList()
        assertEquals(98, l.size)
        val actions = l.map { it.actions }.flatten()
        assertEquals(98, actions.filterIsInstance<PriceBar>().size)
    }

    @Test
    fun background() = runBlocking {
        val feed = TestData.feed()
        val size = feed.toList().size

        val channel = EventChannel(100)
        assertFalse(channel.closed)
        val job = feed.runBackgroud(channel)
        job.join()
        assertTrue(channel.closed)
        assertTrue(job.isCompleted)

        var e = 0
        for (x in channel) {
            assertTrue(x.actions.isNotEmpty())
            e++
        }
        assertEquals(size, e)
        assertTrue(channel.closed)
    }


}