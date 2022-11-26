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
import org.junit.jupiter.api.assertThrows
import org.roboquant.TestData
import org.roboquant.common.Size
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ModifyOrderHandlerTest {

    private val asset = TestData.usStock()


    @Test
    fun testUpdate() {
        val order1 = LimitOrder(asset, Size(100), 10.0)
        val handler = LimitOrderHandler(order1)

        val order2 = LimitOrder(asset, Size(100), 11.0)
        assertTrue(handler.update(order2, Instant.now()))

        assertEquals(order2.limit, handler.order.limit)
    }


    @Test
    fun testFailingUpdate() {
        val order1 = MarketOrder(asset, 100)
        val moc = MarketOrderHandler(order1)

        val order2 = LimitOrder(asset, Size(100), 10.0)
        assertThrows<IllegalArgumentException> {
            UpdateOrder(moc.state, order2)
        }

    }

    @Test
    fun testCancellation() {
        val order1 = MarketOrder(asset, 100)
        val moc = MarketOrderHandler(order1)

        val order = CancelOrder(moc.state, id=12345)

        val cmd = CancelOrderHandler(order)
        cmd.execute(listOf(moc), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.state.status)
        assertEquals(OrderStatus.CANCELLED, moc.state.status)
        assertEquals("type=CANCEL id=12345 tag= size=100, tif=GTC", order.toString())
    }


    @Test
    fun testCancellationAll() {
        val order1 = MarketOrder(asset, 100)
        val moc1 = MarketOrderHandler(order1)
        val moc2 = MarketOrderHandler(order1)

        val order = CancelAllOrder(id=12345)

        val cmd = CancelAllOrderHandler(order)
        cmd.execute(listOf(moc1, moc2), Instant.now())
        assertEquals(OrderStatus.COMPLETED, cmd.state.status)
        assertEquals(OrderStatus.CANCELLED, moc1.state.status)
        assertEquals(OrderStatus.CANCELLED, moc2.state.status)
        assertEquals("type=CANCELALL id=12345 tag= ", order.toString())
    }


}