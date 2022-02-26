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
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals

internal class CombinedOrderHandlerTest {

    private val asset = TestData.usStock()

    private fun pricing(price: Number): Pricing {
        val engine = NoSlippagePricing()
        return engine.getPricing(TradePrice(asset, price.toDouble()), Instant.now())
    }

    @Test
    fun testOCO() {
        val order1 = MarketOrder(asset, 100.0)
        val order2 = MarketOrder(asset, 50.0)
        val order = OCOOrder(order1, order2)
        val cmd = OCOOrderHandler(order)
        val executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(100.0, executions.first().size)
    }

    @Test
    fun testOCO2() {
        val order1 = LimitOrder(asset, 100.0, 90.0)
        val order2 = MarketOrder(asset, 50.0)
        val order = OCOOrder(order1, order2)
        val cmd = OCOOrderHandler(order)
        val executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(50.0, executions.first().size)
    }


    @Test
    fun testOTO() {
        val order1 = MarketOrder(asset, 100.0)
        val order2 = MarketOrder(asset, 50.0)
        val order = OTOOrder(order1, order2)
        val cmd = OTOOrderHandler(order)
        val executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(2, executions.size)
        assertEquals(100.0, executions.first().size)
        assertEquals(50.0, executions.last().size)
    }

    @Test
    fun testBracker() {
        val entry = MarketOrder(asset, 50.0)
        val profit = LimitOrder(asset, -50.0, 110.0)
        val loss = StopOrder(asset, -50.0, 95.0)
        val order = BracketOrder(entry, profit, loss)
        val cmd = BracketOrderHandler(order)

        var executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(1, executions.size)

        executions = cmd.execute(pricing(102), Instant.now())
        assertEquals(0, executions.size)

        executions = cmd.execute(pricing(111), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(OrderStatus.COMPLETED, cmd.status)

    }



}