package org.roboquant.orders


import org.roboquant.TestData
import org.junit.Test
import kotlin.test.assertEquals


internal class OrderRequestTest {


    @Test
    fun test() {
        val asset = TestData.usStock()
        var or:SingleOrder = MarketOrder(asset,100.0)
        assertEquals(asset, or.asset)

        or = MarketOrder(asset, 100.0)
        assertEquals(asset, or.asset)

        or = LimitOrder(asset, 100.0, 120.0)
        assertEquals(asset, or.asset)

        or = StopOrder(asset, 100.0, 110.0)
        assertEquals(asset, or.asset)

        or = StopLimitOrder(asset, 100.0, 110.0, 120.0)
        assertEquals(asset, or.asset)

    }
}