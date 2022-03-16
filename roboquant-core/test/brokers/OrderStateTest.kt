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

package org.roboquant.brokers

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.common.days
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class OrderStateTest {

    @Test
    fun basis() {
        val order = TestData.usMarketOrder()
        var state = OrderState(order)
        assertEquals(OrderStatus.INITIAL, state.status)

        val t = Instant.now()
        state = state.copy(t)
        assertEquals(OrderStatus.ACCEPTED, state.status)

        val t2 = t + 1.days
        state = state.copy(t2, OrderStatus.COMPLETED)
        assertEquals(OrderStatus.COMPLETED, state.status)
        assertEquals(t, state.openedAt)
        assertEquals(t2, state.closedAt)

    }

    @Test
    fun internalAccountExtensions() {
        val order = TestData.usMarketOrder()
        val account = TestData.internalAccount()
        account.rejectOrder(order, Instant.now())
        assertContains(account.closedOrders.map { it.order }, order)

        val order2 = TestData.usMarketOrder()
        account.acceptOrder(order2, Instant.now())
        assertContains(account.openOrders.values.map { it.order }, order2)
    }

    @Test
    fun collectionExtensions() {
        val orders = listOf(TestData.usMarketOrder(), TestData.usMarketOrder())
        val states = orders.initialOrderState
        assertContains(states.map { it.order }, orders.first())
    }
}