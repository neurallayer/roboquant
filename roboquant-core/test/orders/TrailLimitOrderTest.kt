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

package org.roboquant.orders

import org.junit.Test
import org.roboquant.TestData
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse


internal class TrailLimitOrderTest {

    @Test
    fun testTrailOrder() {
        val asset = TestData.usStock()
        val order = TrailOrder(asset, -100.0, 0.01)
        order.status = OrderStatus.ACCEPTED
        order.placed = Instant.now()

        var e1 = order.execute(100.0, Instant.now())
        assertEquals(0.0, e1)
        assertFalse { order.executed }

        e1 = order.execute(101.0, Instant.now())
        assertEquals(0.0, e1)
        assertFalse { order.executed }

        e1 = order.execute(99.0, Instant.now())
        assertEquals(order.quantity, e1)

        val order2 = order.clone()
        assertEquals(order.quantity, order2.quantity)
    }


    @Test
    fun testTrailLimitOrder() {
        val asset = TestData.usStock()
        val order = TrailLimitOrder(asset, -100.0, 0.01, 0.01)
        order.status = OrderStatus.ACCEPTED
        order.placed = Instant.now()

        var e1 = order.execute(100.0, Instant.now())
        assertEquals(0.0, e1)
        assertFalse { order.executed }

        e1 = order.execute(101.0, Instant.now())
        assertEquals(0.0, e1)
        assertFalse { order.executed }

        e1 = order.execute(99.0, Instant.now())
        assertEquals(-100.0, e1)

        assertEquals(0.01, order.limitOffset)
        val order2 = order.clone()
        assertEquals(order.quantity, order2.quantity)
    }

    @Test
    fun from() {
        val usOrder = TestData.usMarketOrder()
        val order = TrailLimitOrder.from(usOrder, 0.02, 0.01)
        assertEquals(usOrder.quantity, - order.quantity)
    }

}