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

package org.roboquant.common


import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import kotlin.test.*

internal class AssetTest {

    @Test
    fun basic() {
        val a = Asset("ABC")
        val b = Asset("XYZ")
        val c = Asset("ABC")
        val d = Asset("ABC", AssetType.BOND)

        assertEquals("ABC", a.symbol)
        assertEquals(a, c)
        assertEquals(a, a)
        assertNotEquals(b, c)
        assertNotEquals(c, d)
    }

    @Test
    fun sorting() {
        val a = Asset("ABC")
        val b = Asset("ABD")
        assertTrue(b > a)
        assertFalse(a > a)
    }

    @Test
    fun testAssetTypeConstructors() {
        val a = Asset.optionContract("SPX", LocalDate.parse("2014-11-22"), 'P', BigDecimal("19.50"))
        assertEquals("SPX   141122P00019500", a.symbol)

        val b = Asset.futureContract("GC", Month.DECEMBER, 18)
        val b2 = Asset.futureContract("GC", Month.DECEMBER, 2018)
        assertEquals("GCZ18", b.symbol)
        assertEquals(b, b2)

        assertEquals("GCF20", Asset.futureContract("GC", Month.JANUARY, 2020).symbol)

        val c = Asset.forexPair("EUR_USD")
        assertEquals("EUR/USD", c.symbol)

        assertThrows<UnsupportedException> {
            Asset.forexPair("dsgTYUYUSDD")
        }

        val d = Asset.forexPair("EURUSD")
        assertEquals("EUR/USD", d.symbol)

        val cr = Asset.crypto("BTC", "USDT", "COINBASE")
        assertEquals("BTC/USDT", cr.symbol)

    }

    @Test
    fun testCollection() {
        val a = Asset("TEST", AssetType.STOCK, "EUR", "AEB")
        val b = Asset("TEST2", AssetType.STOCK, "USD", "NYSE")
        val assets = listOf(a, b)

        assertEquals(a, assets.getBySymbol("TEST"))
        assertEquals(a, assets.findBySymbols("TEST").first())
        assertEquals(b, assets.findByExchanges("NYSE")[0])
        assertEquals(b, assets.findByCurrencies("USD")[0])

        val asset = assets.random(1)
        assertTrue { assets.containsAll(asset) }

        val s = assets.summary()
        assertTrue { s.content.isNotEmpty() }
        assertContains(s.toString(), "TEST")
        assertContains(s.toString(), "TEST2")

        val e = emptyList<Asset>()
        assertContains(e.summary().toString(), "EMPTY")
    }

    @Test
    fun contractValue() {
        val a = Asset("ABC", multiplier = 100.0)
        assertEquals(25000.0.USD, a.value(Size(10), 25.0))

        val b = Asset("ABC")
        assertEquals((-250.0).USD, b.value(Size(-10), 25.0))
    }

    @Test
    fun contractSize() {
        val a = Asset("ABC", multiplier = 100.0)
        val s = a.contractSize(1000.0, 1.0)
        assertEquals(Size(10), s)

        val s2 = a.contractSize(1000.0, 1.0, 4)
        assertEquals(Size(10), s2)

        // decimal fractions cannot be negative
        assertThrows<java.lang.IllegalArgumentException> { a.contractSize(250.0, 1.0, -1) }
    }

}
