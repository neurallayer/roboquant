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
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Asset
import org.roboquant.common.Currency
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.assertEquals

internal class InternalAccountTest {


    @Test
    fun internalAccountOrders() {
        val account = InternalAccount(Currency.USD)
        val order = MarketOrder(Asset("Test"), 100)
        account.initializeOrders(listOf(order))

        assertEquals(OrderStatus.INITIAL, account.orders.first().status)
        assertEquals(1, account.orders.size)

        val opened = Instant.now()
        account.updateOrder(order, opened, OrderStatus.ACCEPTED)
        assertEquals(OrderStatus.ACCEPTED, account.orders.first().status)
        assertEquals(1, account.orders.size)

        val closed = Instant.now()
        account.updateOrder(order, closed, OrderStatus.COMPLETED)
        assertEquals(0, account.orders.size)
        assertEquals(OrderStatus.COMPLETED, account.toAccount().closedOrders.first().status)
    }

    @Test
    fun internalAccountErrors() {
        val account = InternalAccount(Currency.USD)
        val order = MarketOrder(Asset("Test"), 100)

        // Should fail because order is unknown
        assertThrows<NoSuchElementException> {
            account.updateOrder(order, Instant.now(), OrderStatus.ACCEPTED)
        }

        account.initializeOrders(listOf(order))
        account.updateOrder(order, Instant.now(), OrderStatus.COMPLETED)

        // Should fail because order status is closed
        assertThrows<NoSuchElementException> {
            account.updateOrder(order, Instant.now(), OrderStatus.COMPLETED)
        }

    }


}