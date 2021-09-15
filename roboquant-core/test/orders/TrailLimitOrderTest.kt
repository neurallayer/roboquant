package org.roboquant.orders

import org.roboquant.TestData
import org.junit.Test
import kotlin.test.assertTrue


internal class TrailLimitOrderTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val order = TrailLimitOrder(asset, 100.0, 0.01, -0.01)

        assertTrue(order.quantity == 100.0)
    }

}