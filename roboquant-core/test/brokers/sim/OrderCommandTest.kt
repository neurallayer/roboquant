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
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.StopLimitOrder
import org.roboquant.orders.StopOrder
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFails

internal class OrderCommandTest {

    private val asset = TestData.usStock()

    private fun pricing(price: Number): Pricing {
        val engine = NoSlippagePricing()
        return engine.getPricing(TradePrice(asset, price.toDouble()), Instant.now())
    }

    @Test
    fun testMarketOrder() {
        val order = MarketOrder(asset, 100.0)
        val cmd = MarketOrderCommand(order)
        var executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(1, executions.size)

        assertFails {
            executions = cmd.execute(pricing(100), Instant.now())
        }
    }


    @Test
    fun testStopOrder() {
        val order = StopOrder(asset, -10.0, 99.0)
        val cmd = StopOrderCommand(order)
        var executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)

        executions = cmd.execute(pricing(98), Instant.now())
        assertEquals(1, executions.size)
    }

    @Test
    fun testLimitOrder() {
        val order = LimitOrder(asset, 10.0, 99.0)
        val cmd = LimitOrderCommand(order)
        var executions = cmd.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)

        executions = cmd.execute(pricing(98), Instant.now())
        assertEquals(1, executions.size)
    }


    @Test
    fun testStopLimitOrder() {
        val order = StopLimitOrder(asset, -10.0, 100.0, 98.0)
        val cmd = StopLimitOrderCommand(order)

        var executions = cmd.execute(pricing(101), Instant.now())
        assertEquals(0, executions.size)

        executions = cmd.execute(pricing(97), Instant.now())
        assertEquals(0, executions.size)

        executions = cmd.execute(pricing(99), Instant.now())
        assertEquals(1, executions.size)
    }


}