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

package org.roboquant.alpaca

import org.roboquant.common.*
import org.roboquant.feeds.*
import java.time.Duration
import kotlin.test.*

internal class AlpacaHistoricFeedTestIT {

    private val timeframe = Timeframe.parse("2022-11-14T00:00:00Z", "2022-11-18T23:00:00Z")
    private val timeframe2 = Timeframe.parse("2022-11-18T20:00:00Z", "2022-11-18T20:15:00Z")

    @Test
    fun testHistoricFeed() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = AlpacaHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.isNotEmpty())
        assertTrue(assets.findByCurrencies("USD").isNotEmpty())
        assertTrue(assets.any { it.type == AssetType.STOCK })
        assertTrue(assets.any { it.type == AssetType.CRYPTO })
        feed.close()
    }

    private inline fun <reified T : PriceAction> testResult(feed: AlpacaHistoricFeed) {
        assertTrue(timeframe.contains(feed.timeline.first()))
        assertTrue(timeframe.contains(feed.timeline.last()))

        val actions = feed.filter<PriceAction>()
        val action = actions.first().second
        assertTrue(action is T)
        assertEquals("A", action.asset.symbol)
        feed.close()
        assertTrue(feed.assets.isEmpty())
    }

    @Test
    fun testHistoricQuotes() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = AlpacaHistoricFeed()
        feed.retrieveStockQuotes("A", timeframe = timeframe2)
        testResult<PriceQuote>(feed)
    }

    @Test
    fun testHistoricTrades() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = AlpacaHistoricFeed()
        feed.retrieveStockTrades("A", timeframe = timeframe2)
        testResult<TradePrice>(feed)
    }

    @Test
    fun testHistoricBars() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = AlpacaHistoricFeed()
        feed.retrieveStockPriceBars("A", timeframe = timeframe)
        testResult<PriceBar>(feed)
    }

    @Test
    fun testHistoricBarsWithDuration() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = AlpacaHistoricFeed()
        feed.retrieveStockPriceBars("AAPL", timeframe= timeframe, barDuration = 5, barPeriod = BarPeriod.MINUTE)
        val actions = feed.filter<PriceAction>()
        assertEquals(5, Duration.between(actions[0].first, actions[1].first).toMinutes())
    }

}