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

package org.roboquant.avro

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.common.Config
import org.roboquant.common.symbols
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.filter
import java.time.Instant
import kotlin.io.path.div
import kotlin.test.*


class AvroFeedIT {

    @Test
    fun predefinedSP500() {
        val feed = AvroFeed.sp500()
        assertTrue(feed.assets.size > 490)
        assertTrue(feed.timeframe.start >= Instant.parse("2016-01-01T00:00:00Z"))
        assertContains(feed.assets.symbols, "AAPL")
        assertDoesNotThrow {
            var found = false
            feed.filter<PriceBar> { found = true;false }
            assertTrue(found)
        }
    }

    @Test
    fun predefinedQuotes() {
        val feed = AvroFeed.sp500Quotes()
        assertTrue(feed.assets.size >= 490)
        assertContains(feed.assets.symbols, "AAPL")
        assertDoesNotThrow {
            var found = false
            feed.filter<PriceQuote> { found = true;false }
            assertTrue(found)
        }
    }

    @Test
    fun predefinedForex() {
        val feed = AvroFeed.forex()
        assertEquals(1, feed.assets.size)
        assertContains(feed.assets.symbols, "EUR_USD")
        assertDoesNotThrow {
            var found = false
            feed.filter<PriceBar> { found = true;false }
            assertTrue(found)
        }
    }

    @Test
    fun loadFromStore() {
        val fileName = "sp500_pricebar_v6.0.avro"
        val file = (Config.home / fileName).toFile()
        file.delete()
        assertFalse(file.exists())

        // Force loading of file
        AvroFeed.sp500()
        val file2 = (Config.home / fileName).toFile()
        assertTrue(file2.exists())
    }


}
