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

package org.roboquant.brokers.sim

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.common.Size
import org.roboquant.orders.CancelOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.UpdateOrder
import java.time.Instant
import kotlin.test.assertEquals

internal class ModifyOrderHandlerTest {

    private val asset = TestData.usStock()

    @Test
    fun testUpdate() {
        val order1 = MarketOrder(asset, 100)
        val moc = MarketOrderHandler(order1)

        val order2 = MarketOrder(asset, 50)
        val order = UpdateOrder(moc.state, order2)

        val cmd = UpdateOrderHandler(order)
        cmd.execute(listOf(moc), Instant.now())
        assertEquals(Size(50), moc.order.size)
    }

    @Test
    fun testCancellation() {
        val order1 = MarketOrder(asset, 100)
        val moc = MarketOrderHandler(order1)

        val order = CancelOrder(moc.state)

        val cmd = CancelOrderHandler(order)
        cmd.execute(listOf(moc), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.state.status)
        assertEquals(OrderStatus.EXPIRED, moc.state.status)
    }

}