/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.brokers

import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Asset
import org.roboquant.orders.*
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OrderStateTest {

    private fun validate(state: OrderState, status: OrderStatus, opened: Instant, closed: Instant) {
        assertEquals(status, state.status)
        assertEquals(opened, state.openedAt)
        assertEquals(closed, state.closedAt)
    }

    @Test
    fun orderStateExtensions() {
        val order = MarketOrder(Asset("TEST"), 100)
        var state = OrderState(order)
        var c = listOf(state).cancel()
        assertEquals(1, c.size)

        state = state.update(OrderStatus.COMPLETED, Instant.now())
        c = listOf(state).cancel()
        assertEquals(0, c.size)
    }

    @Test
    fun regularFlow() {
        val order = MarketOrder(Asset("TEST"), 100)
        var state = OrderState(order)
        assertTrue(state.open)
        validate(state, OrderStatus.INITIAL, Instant.MAX, Instant.MAX)

        val opened = Instant.now()
        state = state.update(OrderStatus.ACCEPTED, opened)
        assertTrue(state.open)
        validate(state, OrderStatus.ACCEPTED, opened, Instant.MAX)

        val closed = Instant.now()
        state = state.update(OrderStatus.COMPLETED, closed)
        assertTrue(state.closed)
        validate(state, OrderStatus.COMPLETED, opened, closed)
    }

    @Test
    fun errorFlow() {
        val order = MarketOrder(Asset("TEST"), 100)
        var state = OrderState(order)

        val closed = Instant.now()
        state = state.update(OrderStatus.COMPLETED, closed)
        validate(state, OrderStatus.COMPLETED, closed, closed)

        // Cannot update an already closed order
        assertThrows<IllegalStateException> {
            state.update(OrderStatus.COMPLETED, closed)
        }

    }

    @Test
    fun assets() {
        val asset = Asset("TEST")
        val order = MarketOrder(asset, 100)
        val states = listOf(OrderState(order))
        assertEquals(setOf(asset), states.assets)
    }


}
