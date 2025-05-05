/*
 * Copyright 2020-2025 Neural Layer
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
import org.roboquant.common.Stock
import org.roboquant.common.PriceBar
import org.roboquant.common.PriceQuote
import kotlin.test.Test
import kotlin.test.assertEquals


internal class PriceParserTest {

    @Test
    fun priceBarParser() {
        val parser = PriceBarParser()

        assertThrows<IllegalArgumentException> {
            parser.init(listOf("time"))
        }

        assertDoesNotThrow {
            parser.init(listOf("open", "high", "low", "close", "volume"))
        }

        val asset = Stock("TEST")
        val pb = parser.parse(listOf("10.0", "11.0", "9.0", "10.50", ""), asset)
        assertEquals(11.0, pb.high)
    }

    @Test
    fun priceBarParser2() {
        val parser = PriceBarParser(open = 4, high = 3, low = 2, close = 1, volume = 5)
        val asset = Stock("TEST")
        val pb = parser.parse(listOf("dummy", "10.50", "9.0", "11.0", "10", "100"), asset)

        val e = PriceBar(asset, 10.0, 11.0, 9.0, 10.50, 100)
        assertEquals(e.ohlcv.toList(), pb.ohlcv.toList())
    }

    @Test
    fun priceQuoteParser() {
        val parser = PriceQuoteParser()

        assertThrows<IllegalArgumentException> {
            parser.init(listOf("time"))
        }

        assertDoesNotThrow {
            parser.init(listOf("ask", "bid", "asksize", "bidsize"))
        }

        val asset = Stock("TEST")
        val pb = parser.parse(listOf("11.0", "10.0", "1000", "2000"), asset)

        val e = PriceQuote(asset, 11.0, 1000.0, 10.0, 2000.0)
        assertEquals(e, pb)
    }


}
