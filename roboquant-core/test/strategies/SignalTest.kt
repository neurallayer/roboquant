package org.roboquant.strategies


import org.roboquant.common.Asset
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class SignalTest {

    @Test
    fun signalTest() {
        val c = Asset("AAPL")
        val s = Signal(c, Rating.BUY)

        assertEquals(s.asset, c)
        assertTrue( s.takeProfit.isNaN())
        assertTrue( s.takeProfit.isNaN())
        assertTrue( s.probability.isNaN())
        assertEquals(s.rating, Rating.BUY)

        val mo = s.toMarketOrder(100.0)
        assertEquals(100.0, mo.quantity)

        assertFails {
            s.toLimitOrder(100.0)
        }

        val s2 = Signal(c, Rating.SELL, takeProfit = 110.0)
        assertTrue(s2.conflicts(s))
        assertTrue(! s2.conflicts(s2))

        val lo = s2.toLimitOrder(100.0)
        assertEquals(110.0, lo.limit )

    }


}