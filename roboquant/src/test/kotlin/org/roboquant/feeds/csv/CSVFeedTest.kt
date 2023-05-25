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
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.div
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
            assetBuilder = {
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
            assetBuilder = {
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
    fun mt5PriceBar() {
        val dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

        fun parse(line: List<String>, asset: Asset): Instant {
            val text = line[0] + " " + line[1]
            val dt = LocalDateTime.parse(text, dtf)
            return asset.exchange.getInstant(dt)
        }

        val feed = CSVFeed(TestData.dataDir() + "MT5/TEST_M1.csv") {
            assetBuilder = { Asset("MSFT") }
            separator = '\t'
            timeParser = TimeParser { a,b -> parse(a,b) }
        }
        val assets = feed.assets
        assertEquals(1, assets.size)
        assertEquals(16, feed.timeline.size)
    }

    @Test
    fun mt5PriceQuote() {
        val dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS")

        fun parse(line: List<String>, asset: Asset): Instant {
            val text = line[0] + " " + line[1]
            val dt = LocalDateTime.parse(text, dtf)
            return asset.exchange.getInstant(dt)
        }

        val feed = CSVFeed(TestData.dataDir() + "MT5/TEST_QUOTES.csv") {
            assetBuilder = { Asset("MSFT") }
            separator = '\t'
            timeParser = TimeParser { a,b -> parse(a,b) }
            priceParser = PriceQuoteParser()
        }
        val assets = feed.assets
        assertEquals(1, assets.size)
        assertEquals(49, feed.timeline.size)
    }


    @Test
    fun histData() {
        val feed = CSVFeed.fromHistData(TestData.dataDir() + "/HISTDATA")
        assertEquals(1, feed.assets.size)
        assertEquals(20, feed.timeline.size)
        assertEquals(Timeframe.parse("2023-05-01T00:00:00Z", "2023-05-01T00:19:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun stooqData() {
        val feed = CSVFeed.fromStooq(TestData.dataDir() + "/STOOQ")
        assertEquals(1, feed.assets.size)
        assertEquals(20, feed.timeline.size)
        assertEquals(Timeframe.parse("1984-09-07T20:00:00Z", "1984-10-04T20:00:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun noAssets() {
        assertFailsWith<IllegalArgumentException> {
            CSVFeed(TestData.dataDir() + "NON_Existing_DIR")
        }

        val feed1 = CSVFeed(TestData.dataDir() + "US") {
            fileExtension = ".non_existing_ext"
        }
        assertTrue(feed1.assets.isEmpty())
    }


    @Test
    fun customConfig() {
        val template = Asset("TEMPLATE", currencyCode = "USD", exchangeCode = "TEST123")
        val path = Path(TestData.dataDir() + "US") / Path("AAPL.csv")
        val config = CSVConfig()
        config.template = template
        assertEquals(template, config.template)

        val feed = CSVFeed(path) {
            this.template = template
        }
        val first = feed.first().actions.first() as PriceAction
        assertEquals("TEST123", first.asset.exchange.exchangeCode)

        fun CSVConfig.myConfigure() {
            this.template = Asset("TEMPLATE", currencyCode = "USD", exchangeCode = "TEST345")
        }

        val feed2 = CSVFeed(path, configure = CSVConfig::myConfigure)
        val first2 = feed2.first().actions.first() as PriceAction
        assertEquals("TEST345", first2.asset.exchange.exchangeCode)
    }


}