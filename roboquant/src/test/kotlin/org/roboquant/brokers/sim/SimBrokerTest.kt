/*
 * Copyright 2020-2023 Neural Layer
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

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.common.*
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimBrokerTest {

    @Test
    fun basicSimBrokerTest() {
        val broker = SimBroker()
        assertEquals(Wallet(1_000_000.USD), broker.initialDeposit)

        val event = TestData.event()
        val account = broker.place(emptyList(), event)
        assertTrue(account.openOrders.isEmpty())
        assertTrue(account.closedOrders.isEmpty())
        assertTrue(account.trades.isEmpty())
        assertEquals(USD, account.baseCurrency)

        val broker2 = SimBroker(100_000.00.USD.toWallet())
        assertEquals(Wallet(100_000.USD), broker2.initialDeposit)
        assertEquals(USD, broker2.account.baseCurrency)

        val metrics = broker.getMetrics()
        assertTrue(metrics.isEmpty())

        broker2.refresh()
        assertEquals(Wallet(100_000.USD), broker2.initialDeposit)
    }


    @Test
    fun placeSingleCurrencyOrder() {
        val broker = SimBroker()

        val asset = Asset("TEST")
        val order = MarketOrder(asset, 10)
        val price = TradePrice(asset, 100.0)
        val now = Instant.now()
        val event = Event(listOf(price), now)

        val account = broker.place(listOf(order), event)
        assertEquals(1,account.closedOrders.size)
        assertEquals(0, account.openOrders.size)
        val orderState = account.closedOrders.first()
        assertEquals(now, orderState.openedAt)
        assertEquals(now, orderState.closedAt)
        assertEquals(OrderStatus.COMPLETED, orderState.status)
        assertEquals(asset, orderState.asset)

        assertEquals(1, account.trades.size)
        val trade = account.trades.first()
        assertEquals(Size(10), trade.size)
        assertEquals(now, trade.time)

        assertEquals(broker.initialDeposit - trade.totalCost, account.cash)

        assertEquals(1, account.positions.size)
        val pos = account.positions.first()
        assertEquals(Size(10), pos.size)
    }


    @Test
    fun placeMultipleOrders() {
        val er = FixedExchangeRates(USD, EUR to 0.8)
        Config.exchangeRates = er
        val broker = SimBroker()
        val event = TestData.event()
        val orders = listOf(TestData.euMarketOrder(), TestData.usMarketOrder())
        var account = broker.place(orders, event)
        assertEquals(account.closedOrders.size, account.trades.size)
        assertEquals(1, account.openOrders.size)

        account = broker.place(emptyList(), TestData.event2())
        assertEquals(1, account.openOrders.size)
        assertEquals(1, account.assets.size)
        assertEquals(1, account.closedOrders.size)
        assertEquals(1, account.trades.size)
    }


}
