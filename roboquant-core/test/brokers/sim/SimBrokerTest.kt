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

package org.roboquant.brokers.sim

import org.junit.Test
import org.roboquant.TestData
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.assets
import org.roboquant.common.Config
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.orders.closed
import org.roboquant.orders.open
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimBrokerTest {

    @Test
    fun basicSimBrokerTest() {
        val broker = SimBroker()
        val event = TestData.event()
        val account = broker.place(listOf(), event)
        assertTrue(account.orders.isEmpty())

        broker.place(listOf(), event)

        val broker2 = SimBroker.withDeposit(100_000.00)
        assertEquals(USD, broker2.account.baseCurrency)

        val metrics = broker.getMetrics()
        assertTrue(metrics.isEmpty())
    }

    @Test
    fun basicPlaceOrder() {
        val er = FixedExchangeRates(USD, EUR to 0.8)
        Config.exchangeRates = er
        val broker = SimBroker()
        val event = TestData.event()
        val orders = listOf(TestData.euMarketOrder(), TestData.usMarketOrder())
        var account = broker.place(orders, event)
        assertEquals(2, account.orders.size)
        assertEquals(account.orders.closed.size, account.trades.size)
        assertEquals(1, account.orders.open.size)

        account = broker.place(listOf(), TestData.event2())
        assertEquals(1, account.orders.open.size)
        assertEquals(1, account.assets.size)
    }

    @Test
    fun liquidateTest() {
        val broker = SimBroker()
        val event = TestData.event()
        var account = broker.place(listOf(TestData.usMarketOrder()), event)
        assertEquals(1, account.portfolio.assets.size)
        assertEquals(1, account.portfolio.assets.size)

        account = broker.liquidatePortfolio()
        assertEquals(0, account.orders.open.size)
        assertEquals(0, account.portfolio.assets.size)
        assertEquals(2, account.orders.size)

    }


    @Test
    fun advancedPlaceOrder() {
        val er = FixedExchangeRates(USD, EUR to 0.8)
        Config.exchangeRates = er
        val broker = SimBroker()
        val event = TestData.event()
        val orders = listOf(TestData.euMarketOrder(), TestData.usMarketOrder())
        val account = broker.place(orders, event)
        assertEquals(2, account.orders.size)
        assertEquals(1, account.orders.closed.size)
        // assertEquals(1, account.orders.filter { it.status === OrderStatus.INITIAL }.size)

    }


}