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

package org.roboquant.strategies.utils

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceBarSeriesTest {

    @Test
    fun test() {
        val feed = TestData.feed
        val asset = feed.assets.first()
        val pb = PriceBarSeries(asset, 20)
        assertFalse(pb.isAvailable())
        val data = feed.filter<PriceBar> { it.asset === asset }
        for (entry in data) {
            pb.add(entry.second)
        }
        assertTrue(pb.isAvailable())
        assertTrue(pb.typical.isNotEmpty())
    }

    @Test
    fun test2() {
        val feed = TestData.feed
        val asset1 = feed.assets.first()
        val pb = MultiAssetPriceBarSeries(10)
        assertFalse(pb.isAvailable(asset1))
        val data = feed.filter<PriceBar> { it.asset === asset1 }
        for (entry in data) {
            pb.add(entry.second)
        }
        assertTrue(pb.isAvailable(asset1))
    }

    @Test
    fun adjustedClose() {
        val asset = Asset("DEMO")
        val pb = PriceBar.fromAdjustedClose(asset, 10, 11, 9, 10, 5, 100)
        assertEquals(5.0, pb.open)
        assertEquals(5.0, pb.close)
        assertEquals(200.0, pb.volume)
    }




}