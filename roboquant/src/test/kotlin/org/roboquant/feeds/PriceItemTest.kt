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

package org.roboquant.feeds

import org.roboquant.TestData
import org.roboquant.common.Amount
import org.roboquant.common.OrderBook
import org.roboquant.common.PriceBar
import org.roboquant.common.PriceQuote
import org.roboquant.common.Stock
import org.roboquant.common.TradePrice
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PriceItemTest {

    @Test
    fun testPriceQuote() {
        val asset = Stock("DUMMY")
        val p = PriceQuote(asset, 10.0, 1.0, 9.0, 3.0)
        assertEquals(10.0, p.askPrice)
        assertEquals(1.0, p.askSize)
        assertEquals(9.0, p.bidPrice)
        assertEquals(3.0, p.bidSize)
        var price = p.getPrice()
        assertEquals(9.5, price)

        val amt = p.getPriceAmount()
        assertEquals(Amount("USD", 9.5), amt)

        price = p.getPrice("WEIGHTED")
        assertEquals(9.25, price)

        assertEquals(p.askPrice, p.getPrice("ASK"))
        assertEquals(p.bidPrice, p.getPrice("BID"))
        assertEquals(2.0, p.getVolume())
        assertEquals(0.1, p.spread)
    }

    @Test
    fun testOrderBookEmpty() {
        val asset = TestData.euStock()
        val item = OrderBook(asset, emptyList(), emptyList())
        assertTrue(item.asks.isEmpty())
        assertTrue(item.bids.isEmpty())
        assertEquals(0.0, item.getVolume())
    }

    @Test
    fun priceBar() {
        val asset = TestData.euStock()
        val item = PriceBar(asset, 10, 12, 8, 11, 1000)
        assertEquals(10.0, item.getPrice("OPEN"))
        assertEquals(12.0, item.getPrice("HIGH"))
        assertEquals(8.0, item.getPrice("LOW"))
        assertEquals(11.0, item.getPrice("CLOSE"))

        val action2 = PriceBar(asset, 10, 12, 8, 11)
        assertTrue(action2.volume.isNaN())

        assertContains(action2.toString(), asset.symbol)
    }

    @Test
    fun priceBarMethods() {
        val asset = TestData.euStock()
        val item = PriceBar(asset, 10, 12, 8, 11, 1000)
        assertEquals(10.0, item.open)
        assertEquals(12.0, item.high)
        assertEquals(8.0, item.low)
        assertEquals(11.0, item.close)
        assertEquals(1000.0, item.volume)
    }

    @Test
    fun priceBarAdjustClose() {
        val asset = TestData.euStock()
        val pb = PriceBar(asset, 2, 1, 1, 1, 100)
        pb.adjustClose(0.5)
        assertEquals(1.0, pb.open)
        assertEquals(200.0, pb.volume)

        val pb3 = PriceBar(asset, 2, 1, 1, 1)
        pb.adjustClose(0.5)
        assertEquals(Double.NaN, pb3.volume)
    }

    @Test
    fun orderBook() {
        val asset = TestData.euStock()
        val item = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(100.0, 10.0), OrderBook.OrderBookEntry(100.0, 10.0)),
            listOf(OrderBook.OrderBookEntry(100.0, 9.0), OrderBook.OrderBookEntry(100.0, 9.0))
        )
        assertEquals(400.0, item.getVolume())
        assertEquals(2, item.asks.size)
        assertEquals(2, item.bids.size)
        assertEquals(9.5, item.getPrice())
        assertEquals(0.1, item.spread)
        assertEquals(9.0, item.bestBid)
        assertEquals(10.0, item.bestOffer)
    }

    @Test
    fun testTradePrice() {
        val asset = Stock("DUMMY")
        val p = TradePrice(asset, 10.0, 100.0)
        assertEquals(10.0, p.getPrice())
        assertEquals(100.0, p.volume)
        assertEquals(10.0, p.price)
    }

    @Test
    fun testOrderBook() {
        val asset = Stock("DUMMY")
        val p = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(10.0, 11.0), OrderBook.OrderBookEntry(10.0, 11.1)),
            listOf(OrderBook.OrderBookEntry(20.0, 10.0), OrderBook.OrderBookEntry(20.0, 10.1))
        )

        var price = p.getPrice()
        assertEquals(10.55, price)

        price = p.getPrice("WEIGHTED")
        assertEquals(10.4, price)

    }
}
