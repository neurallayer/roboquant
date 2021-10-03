package org.roboquant.common

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class AssetTest {

    @Test
    fun test() {
        val a = Asset("TEST")
        val b = Asset("TEST2")
        assertNotEquals(a, b)

        val s = a.serialize()
        assertTrue(s.isNotEmpty())

        val c = Asset.deserialize(s)
        assertEquals(a, c)
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
    }

}