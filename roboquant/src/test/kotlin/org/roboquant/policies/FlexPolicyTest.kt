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

package org.roboquant.policies

import org.roboquant.TestData
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.*
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

internal class FlexPolicyTest {

    @Test
    fun order() {
        val policy = FlexPolicy()
        val signals = mutableListOf<Signal>()
        val event = Event(emptyList(), Instant.now())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())
    }

    @Test
    fun orderMinPrice() {
        val policy = FlexPolicy(minPrice = 10.USD)
        val asset = Asset("TEST123")
        val signals = listOf(Signal(asset, Rating.BUY))

        val event1 = Event(listOf(TradePrice(asset, 5.0)), Instant.now())
        val account = TestData.usAccount()
        val orders1 = policy.act(signals, account, event1)
        assertTrue(orders1.isEmpty())

        val event2 = Event(listOf(TradePrice(asset, 15.0)), Instant.now())
        val orders2 = policy.act(signals, account, event2)
        assertTrue(orders2.isNotEmpty())
    }


    @Test
    fun order2() {

        class MyPolicy(val percentage: Double = 0.05) : FlexPolicy() {

            override fun createOrder(signal: Signal, size: Size, price: Double): Order {
                val asset = signal.asset
                val direction = if (size > 0) 1.0 else -1.0
                val percentage = percentage * direction

                return BracketOrder(
                    MarketOrder(asset, size),
                    LimitOrder(asset, size, price * (1 + percentage)),
                    StopOrder(asset, size, price * (1 - percentage))
                )
            }
        }

        val policy = MyPolicy()
        val signals = mutableListOf<Signal>()
        val event = Event(emptyList(), Instant.now())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())

    }

    @Test
    fun chaining() {
        val policy = FlexPolicy()
            .resolve(SignalResolution.FIRST)
            .circuitBreaker(10, 1.days)
        val signals = mutableListOf<Signal>()
        val event = Event(emptyList(), Instant.now())
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())
    }


}