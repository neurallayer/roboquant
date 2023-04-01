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

package org.roboquant.polygon

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.toList
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PolygonTest {


    @Test
    fun supportedAssets() {
        Config.getProperty("TEST_POLYGON") ?: return
        val feed = PolygonHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.size > 3000)

        assertDoesNotThrow {
            assets.getBySymbol("AAPL")
            assets.getBySymbol("JPM")
            assets.getBySymbol("TSLA")
        }
    }


    @Test
    fun testHistoricFeed() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = PolygonHistoricFeed()
        val now = Instant.now() - 10.days
        val period = Timeframe(now - 50.days, now)
        feed.retrieve("IBM", "AAPL", timeframe = period)
        assertTrue(feed.timeline.isNotEmpty())
        assertEquals(2, feed.assets.size)
        assertContains(feed.assets.map { it.symbol }, "IBM")
    }

    @Test
    fun testAssets() {
        Config.getProperty("TEST_POLYGON") ?: return
        val feed = PolygonHistoricFeed()
        val assets = feed.availableAssets
        assertContains(assets.map { it.symbol }, "AAPL")
    }

    @Test
    fun testLiveFeed() = runBlocking {
        Config.getProperty("TEST_POLYGON") ?: return@runBlocking
        val feed = PolygonLiveFeed()
        feed.subscribe("IBM", "AAPL")
        val actions = feed.filter<PriceAction>(timeframe = Timeframe.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.disconnect()
    }



    @Test
    fun testFundamentalsFeed() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val feed = PolygonFundamentalsFeed()
        feed.retrieve("AAPL", "TSLA")
        assertEquals(2, feed.assets.size)
        assertTrue(feed.timeline.isNotEmpty())
        assertTrue(! feed.timeframe.isInfinite())
        assertContains(feed.assets.symbols, "AAPL")
        assertContains(feed.assets.symbols, "TSLA")

        assertTrue(feed.toList().flatMap { it.actions }.all { it is SecFiling })
    }
}