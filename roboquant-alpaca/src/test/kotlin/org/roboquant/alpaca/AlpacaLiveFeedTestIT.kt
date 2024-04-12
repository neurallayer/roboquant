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

package org.roboquant.alpaca

import org.junit.jupiter.api.assertThrows
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AlpacaLiveFeedTestIT {

    private val liveTestTime = 30.seconds

    @Test
    fun test() {
        Config.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        val assets = feed.availableAssets
        assertContains(assets.map { it.symbol }, "AAPL")
        feed.subscribeStocks("AAPL")
        assertContains(feed.assets.symbols, "AAPL")
        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first()
            assertEquals("AAPL", action.second.asset.symbol)
            assertTrue(action.second is PriceBar)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }

    @Test
    fun test2() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        feed.subscribeStocks("AAPL")

        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first().second
            assertTrue(action is PriceBar)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }

    @Test
    fun test4() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        val symbol = feed.availableAssets.first { it.type == AssetType.CRYPTO }.symbol
        feed.subscribeCrypto(symbol)
        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first().second
            assertTrue(action is PriceBar)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }

    @Test
    fun test3() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()

        assertThrows<IllegalArgumentException> {
            // Already connected
            feed.connect()
        }
        val symbols = feed.availableStocks.take(5).symbols
        feed.subscribeStocks(*symbols)

        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first().second
            assertTrue(action is PriceBar)
            val foundSymbols = actions.map { it.second.asset.symbol }.distinct()
            assertTrue(foundSymbols.size > 1)
        } else {
            println("No actions found, perhaps exchange is closed?")
        }
    }

}
