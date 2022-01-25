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

import org.junit.Test
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
        assertEquals(Timeframe.INFINITY, feed.timeframe)
    }

    @Test
    fun basic2() {
        val feed = MyFeed2()
        assertEquals(Timeframe.INFINITY, feed.timeframe)
        assertContains(feed.assets, Asset("AAB"))
    }


}