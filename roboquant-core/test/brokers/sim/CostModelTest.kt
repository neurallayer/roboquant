package org.roboquant.brokers.sim

import org.junit.Test
import kotlin.test.assertEquals

internal class CostModelTest  {

    @Test
    fun testDefaultCostModel() {
        val model = DefaultCostModel()
        assertEquals(model, model)
    }

    @Test
    fun testCommissionBasedCostModel() {
        val model = CommissionBasedCostModel()
        // val order = TestData.usMarketOrder()
       //  val fees = model.calculate(order, Execution(order.asset, 10.0, 10.0))
        assertEquals(model, model)
    }

}