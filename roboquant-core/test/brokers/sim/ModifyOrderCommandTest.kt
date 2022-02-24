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

internal class ModifyOrderCommandTest {

    private val asset = TestData.usStock()

    private fun pricing(price: Number): Pricing {
        val engine = NoSlippagePricing()
        return engine.getPricing(TradePrice(asset, price.toDouble()), Instant.now())
    }

    @Test
    fun testUpdate() {
        val order1 = MarketOrder(asset, 100.0)
        val moc = MarketOrderCommand(order1)

        val order2 = MarketOrder(asset, 50.0)
        val order = UpdateOrder(moc.state, order2)

        val cmd = UpdateOrderCommand(order, listOf(moc))
        cmd.execute(pricing(100), Instant.now())
        assertEquals(50.0, moc.order.quantity)
    }


    @Test
    fun testCancellation() {
        val order1 = MarketOrder(asset, 100.0)
        val moc = MarketOrderCommand(order1)

        val order = CancelOrder(moc.state)

        val cmd = CancelOrderCommand(order, listOf(moc))
        cmd.execute(pricing(100), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)
        assertEquals(OrderStatus.EXPIRED, moc.state.status)
    }




}