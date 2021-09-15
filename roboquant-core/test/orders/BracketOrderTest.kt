package org.roboquant.orders

import org.roboquant.TestData
import org.junit.Test
import kotlin.test.assertTrue


internal class BracketOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = BracketOrder(
            MarketOrder(asset, 10.0),
            LimitOrder(asset, -10.0, 101.0),
            StopOrder(asset, -10.0, 99.0),
        )
        assertTrue(order.main is MarketOrder)
    }

}