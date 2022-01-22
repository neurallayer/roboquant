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

package org.roboquant.binance

import org.junit.Test
import org.roboquant.common.Timeframe
import org.roboquant.common.minutes
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BinanceLiveFeedTestIT {

    @Test
    fun testBinanceFeed() {
        System.getProperty("TEST_BINANCE") ?: return

        val feed = BinanceLiveFeed()
        val availableAssets = feed.availableAssets
        assertTrue(availableAssets.isNotEmpty())
        assertContains(feed.availableAssets, "BTCBUSD")

        assertTrue(feed.assets.isEmpty())
        feed.subscribePriceBar("BTCBUSD")
        assertFalse(feed.assets.isEmpty())

        val timeframe = Timeframe.next(5.minutes)
        val prices = feed.filter<PriceBar>(timeframe = timeframe)
        feed.close()

        assertTrue(prices.isNotEmpty())
        assertEquals("BTCBUSD", prices.first().second.asset.symbol)
    }

}

