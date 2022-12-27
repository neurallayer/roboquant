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
import org.roboquant.common.Size
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals

internal class ModifyOrderExecutorTest {

    private val asset = TestData.usStock()


    @Test
    fun testCancelOrderExecutor() {
        val order1 = MarketOrder(asset, 100)
        val exec1 = MarketOrderExecutor(order1)

        val order = CancelOrder(order1)

        val cmd = CancelOrderExecutor(order)
        cmd.execute(exec1, Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)
        assertEquals(OrderStatus.CANCELLED, exec1.status)
    }

    @Test
    fun testUpdateOrderExecutor() {
        val order1 = LimitOrder(asset, Size(100), 110.0)
        val exec1 = LimitOrderExecutor(order1)
        assertEquals(OrderStatus.INITIAL, exec1.status)

        val order2 = LimitOrder(asset, Size(100), 120.0)
        val order = UpdateOrder(order1, order2)

        val cmd = UpdateOrderExecutor(order)
        assertEquals(OrderStatus.INITIAL, cmd.status)

        cmd.execute(exec1, Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.status)
        assertEquals(120.0, exec1.order.limit)
    }


}