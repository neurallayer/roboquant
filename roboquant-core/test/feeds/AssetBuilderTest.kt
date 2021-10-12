package org.roboquant.feeds

import org.junit.Test
import kotlin.test.*

internal class AssetBuilderTest {

    @Test
    fun testStockBuilder() {
        val stock = StockBuilder().invoke("AAPL")
        assertEquals("AAPL", stock.symbol)
    }

    @Test
    fun testFutureBuilder() {
        val future = FutureBuilder().invoke("AAPL")
        assertEquals("AAPL", future.symbol)
    }

    @Test
    fun testForexBuilder() {
        val fx = ForexBuilder().invoke("USD_EUR")
        assertEquals("EUR", fx.currencyCode)
    }

    @Test
    fun testBondBuilder() {
        val bond = BondBuilder().invoke("AAPL")
        assertEquals("AAPL", bond.symbol)
    }


    @Test
    fun testCryptoBuilder() {
        var crypto = CryptoBuilder().invoke("BTC_USD")
        assertEquals("BTCUSD", crypto.symbol)
        assertEquals("USD", crypto.currencyCode)

        crypto = CryptoBuilder().invoke("BTC-USD")
        assertEquals("BTCUSD", crypto.symbol)
        assertEquals("USD", crypto.currencyCode)

        crypto = CryptoBuilder().invoke("BTC123USD")
        assertEquals("BTC123USD", crypto.symbol)
        assertEquals("USD", crypto.currencyCode)
    }

}