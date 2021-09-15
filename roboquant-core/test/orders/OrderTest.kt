package org.roboquant.orders



import org.roboquant.TestData
import kotlin.test.*

internal class OrderTest {

    @Test
    fun createOrders() {
        val asset = TestData.usStock()
        val mo = MarketOrder( asset,100.0)
        assertEquals(100.0, mo.quantity )
        assertTrue(mo.tif is GTC)

        val lo = LimitOrder( asset,100.0, 20.0)
        assertEquals(100.0, lo.quantity )
        assertEquals(20.0, lo.limit )

        val so = StopOrder( asset,100.0, 20.0)
        assertEquals(100.0, so.quantity )
        assertEquals(20.0, so.stop )

        val slo = StopLimitOrder( asset,100.0, 20.0, 25.0)
        assertEquals(100.0, slo.quantity )
        assertEquals(20.0, slo.stop )
        assertEquals(25.0, slo.limit )
    }


}