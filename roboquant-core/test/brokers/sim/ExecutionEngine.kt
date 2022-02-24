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

package org.roboquant.brokers.sim

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.orders.*
import kotlin.test.assertEquals

internal class ExecutionEngineTest {


    @Test
    fun testAddingOrders() {
        val engine = ExecutionEngine(NoSlippagePricing())
        val asset = TestData.usStock()
        var success = engine.add(MarketOrder(asset, 100.0))
        assertEquals(true, success)
        success = engine.add(LimitOrder(asset, 100.0, 200.0))
        assertEquals(true, success)
        success = engine.add(StopOrder(asset, 100.0, 180.0))
        assertEquals(true, success)
        success = engine.add(StopLimitOrder(asset, 100.0, 180.0, 170.0))
        assertEquals(true, success)
        success = engine.add(TrailOrder(asset, 100.0, 0.01))
        assertEquals(true, success)
        success = engine.add(TrailLimitOrder(asset, 100.0, 0.01, -1.0))
        assertEquals(true, success)

    }



    @Test
    fun testAddingOtherOrders() {
        val engine = ExecutionEngine(NoSlippagePricing())
        val asset = TestData.usStock()

        var order: Order = OneCancelsOtherOrder(LimitOrder(asset, 100.0, 200.0), LimitOrder(asset, -100.0, 200.0))
        var success = engine.add(order)
        assertEquals(true, success)

        order = OneTriggersOtherOrder(LimitOrder(asset, 100.0, 200.0), LimitOrder(asset, -100.0, 200.0))
        success = engine.add(order)
        assertEquals(true, success)

        order = BracketOrder(LimitOrder(asset, 100.0, 200.0), LimitOrder(asset, -100.0, 200.0), LimitOrder(asset, -100.0, 200.0))
        success = engine.add(order)
        assertEquals(true, success)
    }


    @Test
    fun testAddingModifyOrders() {
        val engine = ExecutionEngine(NoSlippagePricing())
        val asset = TestData.usStock()

        val origOrder = MarketOrder(asset, 100.0)
        engine.add(origOrder)
        val state = engine.orderCommands.first.state

        var order: Order = UpdateOrder(state, MarketOrder(asset, 50.0, id = origOrder.id))
        var success = engine.add(order)
        assertEquals(true, success)

        order = CancelOrder(state)
        success = engine.add(order)
        assertEquals(true, success)
    }

}