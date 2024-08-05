/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.feeds.csv

import org.roboquant.TestData
import org.roboquant.common.div
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.toList
import kotlin.test.*

internal class LazyCSVFeedTest {

    @Test
    fun basic() {
        val feed = LazyCSVFeed(TestData.dataDir() / "US")
        assertEquals(3, feed.assets.size)
        val list = feed.toList()
        assertEquals(199, list.size)
    }

    @Test
    fun play() {
        val feed = LazyCSVFeed(TestData.dataDir() / "US") {
            fileSkip = listOf("AAPL.csv")
        }
        val priceBars = feed.filter<PriceBar>()
        assertTrue(priceBars.isNotEmpty())
        assertTrue(priceBars[0].first <= priceBars[1].first)
        assertEquals(2, feed.assets.size)
        assertContains(feed.assets, priceBars.first().second.asset)
    }

}
