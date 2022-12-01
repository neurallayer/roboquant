/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.brokers.sim.execution

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.brokers.sim.NoCostPricingEngine
import org.roboquant.common.Size
import org.roboquant.feeds.Event
import org.roboquant.orders.*
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class ExecutionEngineTest {

    @Test
    fun testAddingOrders() {
        val engine = ExecutionEngine(NoCostPricingEngine())
        val asset = TestData.usStock()
        val size = Size(100)
        var success = engine.add(MarketOrder(asset, 100))
        assertEquals(true, success)
        success = engine.add(LimitOrder(asset, size, 200.0))
        assertEquals(true, success)
        success = engine.add(StopOrder(asset, size, 180.0))
        assertEquals(true, success)
        success = engine.add(StopLimitOrder(asset, size, 180.0, 170.0))
        assertEquals(true, success)
        success = engine.add(TrailOrder(asset, size, 0.01))
        assertEquals(true, success)
        success = engine.add(TrailLimitOrder(asset, size, 0.01, -1.0))
        assertEquals(true, success)

    }

    @Test
    fun testAddingOtherOrders() {
        val engine = ExecutionEngine(NoCostPricingEngine())
        val asset = TestData.usStock()
        val size = Size(100)

        var order: Order = OCOOrder(LimitOrder(asset, -size, 200.0), StopOrder(asset, -size, 190.0))
        var success = engine.add(order)
        assertEquals(true, success)

        order = OTOOrder(LimitOrder(asset, size, 200.0), LimitOrder(asset, -size, 200.0))
        success = engine.add(order)
        assertEquals(true, success)

        order = BracketOrder(
            LimitOrder(asset, size, 200.0),
            LimitOrder(asset, -size, 200.0),
            LimitOrder(asset, -size, 200.0)
        )
        success = engine.add(order)
        assertEquals(true, success)
    }


    @Test
    fun testAddingModifyOrders() {
        val engine = ExecutionEngine(NoCostPricingEngine())
        val asset = TestData.usStock()

        val origOrder = LimitOrder(asset, Size(100), 10.0)
        engine.add(origOrder)
        val origOrder2 = LimitOrder(asset, Size(100), 11.0)
        var order: Order = UpdateOrder(origOrder, origOrder2)
        var success = engine.add(order)
        assertEquals(true, success)

        order = CancelOrder(origOrder)
        success = engine.add(order)
        assertEquals(true, success)
    }

    @Test
    fun testAddingCancelOrders() {
        val engine = ExecutionEngine(NoCostPricingEngine())
        val asset = TestData.usStock()

        val origOrder = MarketOrder(asset, 100)
        assertTrue(engine.add(origOrder))

        val order = CancelOrder(origOrder)
        assertTrue(engine.add(order))

        val executions = engine.execute(Event.empty())
        assertEquals(0, executions.size)
        assertEquals(2, engine.orderStates.size)
        engine.orderStates.forEach { println(it) }
        assertTrue(engine.orderStates.all { it.second.closed })
        engine.removeClosedOrders()
        assertEquals(0, engine.orderStates.size)
    }



    @Test
    fun testRegister() {

        ExecutionEngine.unregister<MarketOrder>()

        val order = TestData.usMarketOrder()
        assertFails {
            ExecutionEngine.getExecutor(order)
        }

        ExecutionEngine.register<MarketOrder> { MarketOrderExecutor(it) }
        val executor = ExecutionEngine.getExecutor(order)
        assertEquals("MarketOrderExecutor", executor::class.simpleName)
    }

}