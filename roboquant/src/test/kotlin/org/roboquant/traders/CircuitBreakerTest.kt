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
import org.roboquant.brokers.Account
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Instruction
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CircuitBreakerTest {

    private class MyTrader : Trader {
        override fun create(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
            return listOf(
                MarketOrder(Stock("A"), 10),
                MarketOrder(Stock("B"), 10),
                MarketOrder(Stock("C"), 10)
            )
        }

    }

    @Test
    fun test() {
        val account = TestData.usAccount()
        val time = Instant.now()
        val policy = CircuitBreaker(MyTrader(), 8, 1.hours)
        var orders = policy.create(emptyList(), account, Event.empty(time))
        assertEquals(3, orders.size)

        orders = policy.create(emptyList(), account, Event.empty(time + 30.minutes))
        assertEquals(3, orders.size)

        orders = policy.create(emptyList(), account, Event.empty(time + 50.minutes))
        assertEquals(0, orders.size)

        orders = policy.create(emptyList(), account, Event.empty(time + 51.minutes))
        assertEquals(0, orders.size)

        orders = policy.create(emptyList(), account, Event.empty(time + 120.minutes))
        assertEquals(3, orders.size)

    }
}
