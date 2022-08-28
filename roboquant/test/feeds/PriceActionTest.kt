/*
 * Copyright 2022 Neural Layer
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

package org.roboquant.feeds


import org.roboquant.TestData
import org.roboquant.common.Asset
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class PriceActionTest {

    @Test
    fun testPriceQuote() {
        val asset = Asset("DUMMY")
        val p = PriceQuote(asset, 10.0, 1.0, 9.0, 3.0)
        assertEquals(10.0, p.askPrice)
        assertEquals(1.0, p.askSize)
        assertEquals(9.0, p.bidPrice)
        assertEquals(3.0, p.bidSize)
        var price = p.getPrice()
        assertEquals(9.5, price)

        price = p.getPrice("WEIGHTED")
        assertEquals(9.25, price)

        val q = PriceQuote.fromValues(asset, p.values)
        assertEquals(p, q)
    }

    @Test
    fun testOrderBookEmpty() {
        val asset = TestData.euStock()
        val event = OrderBook(asset, emptyList(), emptyList())
        assertTrue(event.asks.isEmpty())
        assertTrue(event.bids.isEmpty())


    }


    @Test
    fun priceBar() {
        val asset = TestData.euStock()
        val action = PriceBar(asset, 10, 12, 8, 11, 1000)
        assertEquals(10.0, action.getPrice("OPEN"))
        assertEquals(12.0, action.getPrice("HIGH"))
        assertEquals(8.0, action.getPrice("LOW"))
        assertEquals(11.0, action.getPrice("CLOSE"))
        assertEquals(1000.0, action.volume)
    }

    @Test
    fun priceBarAdjusted() {
        val asset = TestData.euStock()
        val pb = PriceBar.fromAdjustedClose(asset, 2, 1, 1, 1, 0.5, 100)
        assertEquals(1.0, pb.open)
        assertEquals(200.0, pb.volume)

        val values = pb.values
        val pb2 = PriceBar.fromValues(pb.asset, values)
        assertEquals(pb.asset, pb2.asset)
        assertEquals(pb.values, pb2.values)
    }

    @Test
    fun orderbook() {
        val asset = TestData.euStock()
        val action = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(100.0, 10.0), OrderBook.OrderBookEntry(100.0, 10.0)),
            listOf(OrderBook.OrderBookEntry(100.0, 9.0), OrderBook.OrderBookEntry(100.0, 9.0))
        )

        val values = action.values
        val action2 = OrderBook.fromValues(action.asset, values)
        assertEquals(action, action2)
    }

    @Test
    fun testTradePrice() {
        val asset = Asset("DUMMY")
        val p = TradePrice(asset, 10.0, 100.0)
        assertEquals(10.0, p.getPrice("DEFAULT"))
        val q = TradePrice.fromValues(asset, p.values)
        assertEquals(p, q)

        assertEquals(100.0, p.volume)
        val r = p * 2.0
        assertEquals(20.0, r.getPrice())
    }


    @Test
    fun testOrderBook() {
        val asset = Asset("DUMMY")
        val p = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(10.0, 11.0), OrderBook.OrderBookEntry(10.0, 11.1)),
            listOf(OrderBook.OrderBookEntry(20.0, 10.0), OrderBook.OrderBookEntry(20.0, 10.1))
        )

        var price = p.getPrice()
        assertEquals(10.55, price)

        price = p.getPrice("WEIGHTED")
        assertEquals(10.4, price)

        val q = OrderBook.fromValues(asset, p.values)
        assertEquals(p, q)
    }
}