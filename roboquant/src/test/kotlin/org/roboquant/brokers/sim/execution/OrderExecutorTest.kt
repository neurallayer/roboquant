/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.brokers.sim.execution

import org.roboquant.TestData
import org.roboquant.common.Size
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OrderExecutorTest {

    private val asset = TestData.usStock()

    private fun pricing(price: Number): PriceItem {
        //val engine = NoCostPricingEngine()
        // return engine.getPricing(TradePrice(asset, price.toDouble()), Instant.now())
        return TradePrice(asset, price.toDouble())
    }



    @Test
    fun testMarketOrder() {
        val order = MarketOrder(asset, 100)
        val executor = MarketOrderExecutor(order)
        val executions = executor.execute(pricing(100), Instant.now())
        assertEquals(1, executions.size)

        val execution = executions.first()
        assertTrue(execution.value != 0.0)
        assertEquals(execution.order.asset.currency, execution.amount.currency)

        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testStopOrder() {
        val order = StopOrder(asset, Size(-10), 99.0)
        val executor = StopOrderExecutor(order)
        var executions = executor.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)

        executions = executor.execute(pricing(98), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(98.0, executions.first().price)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testStopOrder2() {
        val order = StopOrder(asset, Size(-10), 99.0)
        val executor = StopOrderExecutor(order)
        var executions = executor.execute(pricing(102), Instant.now())
        assertEquals(0, executions.size)

        executions = executor.execute(pricing(97), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(97.0, executions.first().price)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testLimitOrder() {
        val order = LimitOrder(asset, Size(10), 99.0)
        val executor = LimitOrderExecutor(order)
        var executions = executor.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(98), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testLimitTrigger() {
        val order = StopLimitOrder(asset, Size(-10), 100.0, 98.0)
        val executor = StopLimitOrderExecutor(order)

        assertFalse(executor.isTriggered(101.0))
        assertTrue(executor.isTriggered(99.0))
        assertTrue(executor.isTriggered(98.0))

        assertTrue(executor.reachedLimit(101.0))
        assertTrue(executor.reachedLimit(99.0))
        assertFalse(executor.reachedLimit(97.0))
    }

    @Test
    fun testStopLimitOrder() {
        val order = StopLimitOrder(asset, Size(-10), 100.0, 98.0)
        val executor = StopLimitOrderExecutor(order)

        var executions = executor.execute(pricing(101), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(97), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(99), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testTrailOrder() {
        val order = TrailOrder(asset, Size(-10), 0.01)
        order.status = OrderStatus.ACCEPTED
        order.openedAt = Instant.now()

        val executor = TrailOrderExecutor(order)
        var executions = executor.execute(pricing(90), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, order.status)

        executions = executor.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(98), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

    @Test
    fun testTrailLimitOrder() {
        val order = TrailLimitOrder(asset, Size(-10), 0.01, -1.0)
        val executor = TrailLimitOrderExecutor(order)
        var executions = executor.execute(pricing(90), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(100), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(95), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, executor.status)

        executions = executor.execute(pricing(99), Instant.now())
        assertEquals(1, executions.size)
        assertEquals(OrderStatus.COMPLETED, executor.status)
    }

}
