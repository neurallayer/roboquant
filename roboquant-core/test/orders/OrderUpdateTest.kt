package org.roboquant.orders


import org.roboquant.TestData
import org.junit.Test
import kotlin.test.assertEquals

internal class OrderUpdateTest {

    @Test
    fun basic() {
        val order = TestData.euMarketOrder()
        val uOrder = MarketOrder(order.asset,100.0)
        val orderUpdate = UpdateOrder(order, uOrder)
        assertEquals(100.0, orderUpdate.updateOrder.quantity)

    }


}