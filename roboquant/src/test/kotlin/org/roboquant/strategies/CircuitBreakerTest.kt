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

package org.roboquant.strategies

import org.roboquant.TestData
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.hours
import org.roboquant.common.minutes
import org.roboquant.common.plus
import org.roboquant.feeds.Event
import org.roboquant.orders.Instruction
import org.roboquant.orders.MarketOrder
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CircuitBreakerTest {

    private class MyStrat : Strategy {
        override fun create(event: Event, account: Account): List<Instruction> {
            val result = mutableListOf<Instruction>()
            repeat(5) {
                val order = MarketOrder(Asset("ABC"), 10)
                result.add(order)
            }
            return result
        }

    }

    @Test
    fun test() {

        val account = TestData.usAccount()
        val time = Instant.now()
        val strat = MyStrat().circuitBreaker(10, 1.hours)
        var orders = strat.create(Event.empty(time), account)
        assertEquals(5, orders.size)

        orders = strat.create(Event.empty(time + 30.minutes), account)
        assertEquals(5, orders.size)

        orders = strat.create(Event.empty(time + 50.minutes), account)
        assertEquals(0, orders.size)

        orders = strat.create(Event.empty(time + 120.minutes), account)
        assertEquals(5, orders.size)
    }
}
