/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class AssetTest {

    @Test
    fun testAssetSerialization() {
        val a = Asset("TEST")
        val b = Asset("TEST2")
        assertNotEquals(a, b)
        assertEquals(a, a)

        val s = Json.encodeToString(a)
        assertTrue(s.isNotEmpty())

        val c = Json.decodeFromString<Asset>(s)
        assertEquals(a, c)
    }

    @Test
    fun testContracts() {
        val a = Asset.optionContract("SPX", LocalDate.parse("2014-11-22"), 'P', "19.50")
        assertEquals("SPX   141122P00019500", a.symbol)

        val b = Asset.futureContract("GC", 'Z', 18)
        assertEquals("GCZ18", b.symbol)

        val c = Asset.forexPair("EUR_USD")
        assertEquals("EUR/USD", c.symbol)

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
    }

    @Test
    fun testFilter() {
        val asset1 = Asset("abc")
        val assets = listOf(asset1, Asset("BCD"), Asset("CDE"))
        var a = assets.filter { AssetFilter.all().filter(it) }
        assertEquals(3, a.size)

        a = assets.filter { AssetFilter.excludeSymbols("ABC").filter(it) }
        assertFalse(asset1 in a)

        a = assets.filter { AssetFilter.includeSymbols("ABC").filter(it) }
        assertTrue(asset1 in a)

        a = assets.filter { AssetFilter.includeCurrencies(Currency.USD).filter(it) }
        assertTrue(asset1 in a)
    }

}