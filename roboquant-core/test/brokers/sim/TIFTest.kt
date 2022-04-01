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

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.days
import org.roboquant.common.seconds
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals

internal class TIFTest {



    class MySingleOrderHandler(order: MarketOrder) : SingleOrderHandler<MarketOrder>(order) {
        var fillPercentage: Double = 1.0

        override fun fill(pricing: Pricing): Execution? {
            if (fillPercentage != 0.0) return Execution(order, fillPercentage* order.quantity, pricing.marketPrice(100.0))
            return null
        }

    }

    private fun pricing(price: Number = 100): Pricing {
        val engine = NoSlippagePricing()
        return engine.getPricing(TradePrice(TestData.usStock(), price.toDouble()), Instant.now())
    }

    private fun getOrderCommand(tif: TimeInForce, fillPercentage: Double = 1.0): MySingleOrderHandler {
        val asset = TestData.usStock()
        val order = MarketOrder(asset, 50.0, tif)
        val result = MySingleOrderHandler(order)
        result.fillPercentage = fillPercentage
        return result
    }

    @Test
    fun testDAY() {
        var cmd = getOrderCommand(DAY())
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)

        cmd = getOrderCommand(DAY(), 0.1)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        cmd.execute(pricing(), Instant.now() + 2.days)
        assertEquals(OrderStatus.EXPIRED, cmd.status)
    }

    @Test
    fun testGTC() {
        var cmd = getOrderCommand(GTC(), 1.0)
        cmd.execute(pricing(120), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)

        cmd = getOrderCommand(GTC(), 0.1)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        cmd.execute(pricing(), Instant.now() + 100.days)
        assertEquals(OrderStatus.EXPIRED, cmd.status)
    }


    @Test
    fun testFOK() {
        var cmd = getOrderCommand(FOK(), 1.0)
        cmd.execute(pricing(120), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)

        cmd = getOrderCommand(FOK(), 0.1)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.EXPIRED, cmd.status)

    }


    @Test
    fun testGTD() {
        val date = Instant.now() + 2.days
        var cmd = getOrderCommand(GTD(date), 1.0)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)

        cmd = getOrderCommand(GTD(date), 0.1)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        cmd.execute(pricing(), Instant.now() + 1.days)
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        cmd.execute(pricing(), Instant.now() + 4.days)
        assertEquals(OrderStatus.EXPIRED, cmd.status)

    }


    @Test
    fun testIOC() {
        var cmd = getOrderCommand(IOC(), 1.0)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)

        cmd = getOrderCommand(IOC(), 0.1)
        cmd.execute(pricing(), Instant.now())
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        cmd.execute(pricing(), Instant.now() + 1.seconds)
        assertEquals(OrderStatus.EXPIRED, cmd.status)

    }


}