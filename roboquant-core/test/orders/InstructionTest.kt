package org.roboquant.orders

import org.roboquant.TestData
import org.junit.Test
import kotlin.test.assertEquals


internal class InstructionTest {

    @Test
    fun test() {
        val asset = TestData.usStock()
        val or =  MarketOrder(asset,100.0)
        assertEquals(asset, or.asset)



        val openOrder = MarketOrder(asset,100.0)
        val oc = CancellationOrder(openOrder)
        assertEquals(OrderStatus.INITIAL, oc.status)

        val ou = UpdateOrder(openOrder, MarketOrder(asset,50.0))
        assertEquals(50.0, ou.updateOrder.quantity)

    }
}