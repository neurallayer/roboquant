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

package org.roboquant.feeds.csv

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.symbols
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LazyCSVFeedTest {

    @Test
    fun play() {
        val feed = LazyCSVFeed(TestData.dataDir() + "US") {
            fileExtension = ".csv"
            fileSkip = listOf("AAPL.csv")
        }
        val priceBars = feed.filter<PriceBar>()
        assertTrue(priceBars.isNotEmpty())
        assertTrue(priceBars[0].first <= priceBars[1].first)
        assertEquals(2, feed.assets.size)
        assertFalse("AAPL" in feed.assets.symbols)
        assertContains(feed.assets, priceBars.first().second.asset)
    }

}