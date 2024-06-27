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
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.Currency
import org.roboquant.feeds.Event
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Instruction
import org.roboquant.strategies.Signal
import kotlin.test.Test
import kotlin.test.assertTrue

internal class TestTraderTest {

    private class MyTrader : Trader {
        override fun create(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
            return emptyList()
        }

    }

    @Test
    fun basic() {
        val policy = MyTrader()
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.create(emptyList(), account, Event.empty())
        assertTrue(orders.isEmpty())
    }

    @Test
    fun order() {
        val policy = TestTrader()

        for (rating in listOf(1.0, -1.0)) {
            val signals = listOf(Signal(TestData.usStock(), rating))
            val event = TestData.event2()
            val account = InternalAccount(Currency.USD).toAccount()
            val orders = policy.create(signals, account, event)
            assertTrue(orders.first() is MarketOrder)
        }

        val signals = listOf(Signal(TestData.usStock(), 0.0))
        val event = TestData.event2()
        val account = InternalAccount(Currency.USD).toAccount()
        val orders = policy.create(signals, account, event)
        assertTrue(orders.isEmpty())
    }

    /*
    private fun getAccount(): Account {
        val iAccount = InternalAccount()
        val asset = Asset("XYZ123")
        iAccount.acceptOrder(MarketOrder(asset, 100), Instant.now())
        assertTrue(iAccount.openOrders.isNotEmpty())
        return iAccount.toAccount()
    }
     */


}
