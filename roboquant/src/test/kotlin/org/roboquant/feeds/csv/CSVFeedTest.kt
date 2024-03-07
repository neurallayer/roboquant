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

import kotlin.test.Test
import org.roboquant.TestData
import org.roboquant.common.*
import org.roboquant.feeds.PriceItem
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class CSVFeedTest {

    @Test
    fun getAssets() {
        val feed = CSVFeed(TestData.dataDir() + "US")
        val assets = feed.assets
        assertEquals(3, assets.size)
        val c2 = assets.getBySymbol("AAPL")
        assertEquals("AAPL", c2.symbol)
        assertEquals(199, feed.timeline.size)
    }

    @Test
    fun getAssetsEU() {
        val feed = CSVFeed(TestData.dataDir() + "EU") {
            template = Asset("TEMPLATE", currency = Currency.EUR, exchange = Exchange.getInstance("AEB"))
        }
        val assets = feed.assets
        assertEquals(3, assets.size)
        val c2 = assets.getBySymbol("ASML")
        assertEquals("ASML", c2.symbol)
    }

    @Test
    fun testIntra() {
        val feed = CSVFeed(TestData.dataDir() + "INTRA")
        val assets = feed.assets
        assertEquals(2, assets.size)
    }

    @Test
    fun getForexCSV() {
        val feed = CSVFeed(TestData.dataDir() + "FX1") {
            assetBuilder = AssetBuilder {
                val str = it.name.removeSuffix(".csv")
                Asset.forexPair(str)
            }
        }
        val assets = feed.assets
        assertEquals(2, assets.size)
        assertEquals(Currency.USD, assets.first().currency)
    }

    @Test
    fun getMinutesCSV() {
        val feed = CSVFeed(TestData.dataDir() + "FX2") {
            assetBuilder = AssetBuilder {
                val str = it.name.removeSuffix(".csv")
                Asset.forexPair(str)
            }
        }
        val assets = feed.assets
        assertEquals(1, assets.size)
        assertEquals(Currency.USD, assets.first().currency)
        assertEquals(9, feed.timeline.size)
    }

    @Test
    fun noAssets() {
        assertFailsWith<IllegalArgumentException> {
            CSVFeed(TestData.dataDir() + "NON_Existing_DIR")
        }

        val feed1 = CSVFeed(TestData.dataDir() + "US") {
            filePattern = ".*non_existing_ext"
        }

        assertEquals(".*non_existing_ext", feed1.config.filePattern)
        assertTrue(feed1.assets.isEmpty())
    }

    @Test
    fun customConfig() {
        val template = Asset("TEMPLATE", currencyCode = "USD", exchangeCode = "TEST123")
        val path = TestData.dataDir() / "US/AAPL.csv"

        val feed = CSVFeed(path) {
            assetBuilder = DefaultAssetBuilder(template)
        }
        val first = feed.first().items.first() as PriceItem
        assertEquals("TEST123", first.asset.exchange.exchangeCode)
    }


}
