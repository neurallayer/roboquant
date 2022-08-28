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

package org.roboquant.feeds

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import java.util.*
import kotlin.test.assertContains
import kotlin.test.assertEquals

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

}