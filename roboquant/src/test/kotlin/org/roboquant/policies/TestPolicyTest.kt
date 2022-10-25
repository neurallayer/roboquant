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
import org.roboquant.brokers.Account
import org.roboquant.brokers.InternalAccount
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.CancelOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.Order
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class TestPolicyTest {

    private class MyPolicy : Policy {
        override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
            return emptyList()
        }

    }

    @Test
    fun basic() {
        val policy = MyPolicy()
        val account = InternalAccount().toAccount()
        val orders = policy.act(emptyList(), account, Event.empty())
        assertTrue(orders.isEmpty())
        assertTrue(policy.getMetrics().isEmpty())
    }

    @Test
    fun order() {
        val policy = TestPolicy()

        for (rating in listOf(Rating.BUY, Rating.SELL)) {
            val signals = listOf(Signal(TestData.usStock(), rating))
            val event = TestData.event2()
            val account = InternalAccount().toAccount()
            val orders = policy.act(signals, account, event)
            assertTrue(orders.first() is MarketOrder)
            assertTrue(policy.getMetrics().isEmpty())
        }

        val signals = listOf(Signal(TestData.usStock(), Rating.HOLD))
        val event = TestData.event2()
        val account = InternalAccount().toAccount()
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())

    }



    private fun getAccount(): Account {
        val iAccount = InternalAccount()
        val asset = Asset("XYZ123")
        iAccount.acceptOrder(MarketOrder(asset, 100), Instant.now())
        assertTrue(iAccount.openOrders.isNotEmpty())
        return iAccount.toAccount()
    }


    @Test
    fun singleOrderCancel() {
        val policy = TestPolicy().singleOrder("cancel")
        val account = getAccount()
        val asset = account.openOrders.first().asset
        val signals = listOf(Signal(asset, Rating.BUY))
        val event = Event(listOf(TradePrice(asset, 1.0)), Instant.now())
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isNotEmpty())
        assertTrue(orders.any { it is CancelOrder })
        assertEquals(2, orders.size)
    }

    @Test
    fun singleOrderRemove() {
        val policy = TestPolicy().singleOrder("remove")
        val account = getAccount()
        val asset = account.openOrders.first().asset
        val signals = listOf(Signal(asset, Rating.BUY))
        val event = Event(listOf(TradePrice(asset, 1.0)), Instant.now())
        val orders = policy.act(signals, account, event)
        assertTrue(orders.isEmpty())
    }
}