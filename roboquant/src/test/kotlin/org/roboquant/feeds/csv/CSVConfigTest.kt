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

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.toList
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


internal class CSVConfigTest {

    @Test
    fun defaultConfig() {
        val config = CSVConfig()
        assertTrue(config.fileSkip.isEmpty())
        assertEquals(true, config.hasHeader)
        assertEquals(',', config.separator)
    }

    @Test
    fun stooq() {
        val config = CSVConfig.stooq()
        val feed = CSVFeed(TestData.dataDir() / "STOOQ", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(20, feed.timeline.size)
        assertEquals(Timeframe.parse("1984-09-07T20:00:00Z", "1984-10-04T20:00:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun stooq2() {
        val config = CSVConfig.stooq()
        val feed = LazyCSVFeed(TestData.dataDir() / "STOOQ", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(20, feed.toList().size)
        assertEquals(Timeframe.INFINITE, feed.timeframe)
    }

    @Test
    fun histData() {
        val config = CSVConfig.histData()
        val feed = CSVFeed(TestData.dataDir() / "HISTDATA", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(20, feed.timeline.size)
        assertEquals(Timeframe.parse("2023-05-01T00:00:00Z", "2023-05-01T00:19:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun yahoo() {
        val config = CSVConfig.yahoo()
        val feed = CSVFeed(TestData.dataDir() / "YAHOO", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals("MSFT", feed.assets.first().symbol)
        assertEquals(9, feed.timeline.size)
        assertEquals(Timeframe.parse("2023-05-26T13:30:00Z", "2023-05-26T13:38:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun kraken() {
        val config = CSVConfig.kraken()
        val feed = CSVFeed(TestData.dataDir() / "KRAKEN", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(4, feed.timeline.size)
        assertEquals(20, feed.toList().map { it.items }.flatten().size)
        assertEquals(Timeframe.parse("2017-08-01T16:03:53Z", "2017-08-01T16:04:00Z").toInclusive(), feed.timeframe)
    }

    @Test
    fun mt5PriceBar() {
        val config = CSVConfig.mt5(timeSpan = 1.minutes)
        val feed = CSVFeed(TestData.dataDir() / "MT5/TEST_M1.csv", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(16, feed.timeline.size)
        assertEquals("TEST", feed.assets.first().symbol)
        assertEquals(Timeframe.parse("2023-01-03T16:00:00Z", "2023-01-05T17:00:00Z").toInclusive(), feed.timeframe)

        val pb = feed.toList().first().items.first()
        assertTrue(pb is PriceBar)
        assertEquals(1.minutes, pb.timeSpan)
    }

    @Test
    fun mt5PriceQuote() {
        val config = CSVConfig.mt5(priceQuote = true)
        val feed = CSVFeed(TestData.dataDir() / "MT5/TEST_QUOTES.csv", config) {}
        assertEquals(1, feed.assets.size)
        assertEquals(49, feed.timeline.size)
        assertEquals("TEST", feed.assets.first().symbol)
        assertEquals(
            Timeframe.parse("2023-04-14T19:00:00.846Z", "2023-04-14T19:01:24.271Z").toInclusive(),
            feed.timeframe
        )
    }

    @Test
    fun basic() {
        val config = CSVConfig()
        assertEquals(USStock("ABN"), config.assetBuilder.build("ABN.csv"))
        assertFalse(config.shouldInclude(File("some_non_existing_file.csv")))
        assertFalse(config.shouldInclude(File("somefile.dummy_extension")))
    }

    @Test
    fun process() {
        val config = CSVConfig()
        val asset = USStock("ABC")
        config.configure(listOf("TIME", "OPEN", "HIGH", "LOW", "CLOSE", "VOLUME"))

        assertDoesNotThrow {
            config.processLine(listOf("2022-01-03", "10.0", "11.0", "9.00", "10.0", "100"), asset)
        }

        assertThrows<NoTradingException> {
            config.processLine(listOf("2022-01-01", "10.0", "11.0", "9.00", "10.0", "100"), asset)
        }

    }


}
