/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.traders

import org.roboquant.TestData
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FlexTraderTest {

    @Test
    fun order() {
        val policy = FlexTrader()
        val signals = mutableListOf<Signal>()
        val event = Event(Instant.now(), emptyList())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.createOrders(signals, account, event)
        assertTrue(orders.isEmpty())
    }

    @Test
    fun order3() {
        val policy = FlexTrader()
        val orders = run(policy)
        assertTrue(orders.isNotEmpty())

        val order = orders.first()
        assertEquals("TEST123", order.asset.symbol)
        assertEquals(Size(204), order.size)
    }

    @Test
    fun orderMinPrice() {
        val policy = FlexTrader {
            minPrice = 10.USD
        }
        val asset = Stock("TEST123")
        val signals = listOf(Signal.buy(asset))

        val event1 = Event(Instant.now(), listOf(TradePrice(asset, 5.0)))
        val account = TestData.usAccount()
        val orders1 = policy.createOrders(signals, account, event1)
        assertTrue(orders1.isEmpty())

        val event2 = Event(Instant.now(), listOf(TradePrice(asset, 15.0)))
        val orders2 = policy.createOrders(signals, account, event2)
        assertTrue(orders2.isNotEmpty())
    }




    private fun run(policy: FlexTrader): List<Order> {
        val asset = Stock("TEST123")
        val signals = listOf(Signal.buy(asset))
        val event = Event(Instant.now(), listOf(TradePrice(asset, 5.0)))
        val account = TestData.usAccount()
        return policy.createOrders(signals, account, event)
    }



    @Test
    fun chaining() {
        val policy = FlexTrader()
            .circuitBreaker(10, 1.days)
        val signals = mutableListOf<Signal>()
        val event = Event(Instant.now(), emptyList())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.createOrders(signals, account, event)
        assertTrue(orders.isEmpty())
    }


}
