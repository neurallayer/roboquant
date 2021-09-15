package org.roboquant.feeds


import org.roboquant.TestData
import org.roboquant.common.Asset
import org.junit.Test
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
    }

    @Test
    fun testOrderBookEmpty() {
        val asset = TestData.euStock()
        val event = OrderBook(asset, listOf(), listOf())
        assertTrue(event.asks.isEmpty())
        assertTrue(event.bids.isEmpty())
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

    }
}