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
import org.roboquant.common.days
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals

internal class TIFTest {

    private val asset = TestData.usStock()

    private fun pricing(price: Number): Pricing {
        val engine = NoSlippagePricing()
        return engine.getPricing(TradePrice(asset, price.toDouble()), Instant.now())
    }

    private fun getOrderCommand(tif: TimeInForce): LimitOrderCommand {
        val order = LimitOrder(asset, 50.0, 100.0, tif)
        return LimitOrderCommand(order)
    }

    @Test
    fun testDAY() {
        val cmd = getOrderCommand(DAY())
        var executions = cmd.execute(pricing(120), Instant.now())
        assertEquals(0, executions.size)
        executions = cmd.execute(pricing(110), Instant.now() + 2.days)
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.EXPIRED, cmd.status)

    }

    @Test
    fun testGTC() {
        val cmd = getOrderCommand(GTC())
        var executions = cmd.execute(pricing(120), Instant.now())
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        executions = cmd.execute(pricing(110), Instant.now() + 2.days)
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.ACCEPTED, cmd.status)

        executions = cmd.execute(pricing(110), Instant.now() + 100.days)
        assertEquals(0, executions.size)
        assertEquals(OrderStatus.EXPIRED, cmd.status)

    }


}