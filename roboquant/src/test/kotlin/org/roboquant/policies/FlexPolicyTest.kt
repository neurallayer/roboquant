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

package org.roboquant.policies

import org.roboquant.TestData
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FlexPolicyTest {

    @Test
    fun order() {
        val policy = FlexPolicy()
        val signals = mutableListOf<Signal>()
        val event = Event(Instant.now(), emptyList())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())
    }


    @Test
    fun order3() {
        val policy = FlexPolicy()
        val orders = run(policy)
        assertTrue(orders.isNotEmpty())

        val order = orders.first()
        assertTrue(order is MarketOrder)
        assertEquals("TEST123", order.asset.symbol)
        assertEquals(Size(204), order.size)
    }

    @Test
    fun orderMinPrice() {
        val policy = FlexPolicy {
            minPrice = 10.USD
        }
        val asset = Asset("TEST123")
        val signals = listOf(Signal.buy(asset))

        val event1 = Event(Instant.now(), listOf(TradePrice(asset, 5.0)))
        val account = TestData.usAccount()
        val orders1 = policy.act(signals, account, event1)
        assertTrue(orders1.isEmpty())

        val event2 = Event(Instant.now(), listOf(TradePrice(asset, 15.0)))
        val orders2 = policy.act(signals, account, event2)
        assertTrue(orders2.isNotEmpty())
    }

    @Test
    fun order2() {

        class MyPolicy(val percentage: Double = 0.05) : FlexPolicy() {

            override fun createOrder(signal: Signal, size: Size, priceAction: PriceItem): Order {
                val asset = signal.asset
                val direction = if (size.isPositive) 1.0 else -1.0
                val percentage = percentage * direction
                val price = priceAction.getPrice(config.priceType)

                return BracketOrder(
                    MarketOrder(asset, size),
                    LimitOrder(asset, size, price * (1 + percentage)),
                    StopOrder(asset, size, price * (1 - percentage))
                )
            }
        }

        val policy = MyPolicy()
        val signals = mutableListOf<Signal>()
        val event = Event(Instant.now(), emptyList())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())

    }


    private fun run(policy: FlexPolicy): List<Order> {
        val asset = Asset("TEST123")
        val signals = listOf(Signal.buy(asset))
        val event = Event(Instant.now(), listOf(TradePrice(asset, 5.0)))
        val account = TestData.usAccount()
        return policy.act(signals, account, event)
    }

    @Test
    fun predefined() {
        val policy = FlexPolicy.bracketOrders()
        val orders = run(policy)
        assertTrue(orders.isNotEmpty())

        val first = orders.first()
        assertTrue(first is BracketOrder)

        val entry = first.entry
        val stop = first.stopLoss
        val profit = first.takeProfit

        assertTrue(entry is MarketOrder)

        assertTrue(stop is StopOrder)
        assertEquals(5.0 * 0.99, stop.stop)

        assertTrue(profit is TrailOrder)
        assertEquals(0.05, profit.trailPercentage)
    }

    @Test
    fun predefined2() {
        val policy = FlexPolicy.limitOrders()
        val orders = run(policy)
        assertTrue(orders.isNotEmpty())

        val first = orders.first()
        assertTrue(first is LimitOrder)
        assertEquals(5 * 0.99, first.limit)
    }

    @Test
    fun chaining() {
        val policy = FlexPolicy()
            .resolve(SignalResolution.FIRST)
            .circuitBreaker(10, 1.days)
        val signals = mutableListOf<Signal>()
        val event = Event(Instant.now(), emptyList())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())
    }


}
