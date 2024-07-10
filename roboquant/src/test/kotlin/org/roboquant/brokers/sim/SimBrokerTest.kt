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

package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.assets
import org.roboquant.common.*
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.Modification
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SimBrokerTest {

    @Test
    fun defaults() {
        val broker = SimBroker()
        assertEquals(Wallet(1_000_000.USD), broker.initialDeposit)
        val account = broker.sync()
        assertEquals(USD, account.baseCurrency)
        assertEquals(Wallet(1_000_000.USD), account.cash)
        assertTrue(account.positions.isEmpty())
        assertTrue(account.orders.isEmpty())
        assertTrue(broker.closedOrders.isEmpty())
        assertTrue(broker.trades.isEmpty())
        assertTrue(broker.realizedPNL().isEmpty())
    }

    @Test
    fun basicSimBrokerTest() {
        val broker = SimBroker()

        val event = TestData.event()
        broker.place(emptyList())
        val account = broker.sync(event)
        assertTrue(account.orders.isEmpty())
        assertTrue(broker.closedOrders.isEmpty())
        assertTrue(broker.trades.isEmpty())
        assertEquals(USD, account.baseCurrency)

        val broker2 = SimBroker(100_000.00.USD.toWallet())
        assertEquals(Wallet(100_000.USD), broker2.initialDeposit)
        assertEquals(USD, broker2.sync().baseCurrency)

        // broker2.refresh()
        assertEquals(Wallet(100_000.USD), broker2.initialDeposit)
    }


    private fun getFilledSimbroker(): SimBroker {
        val broker = SimBroker()
        val asset = Asset("TEST")
        val order = MarketOrder(asset, 10)
        val price = TradePrice(asset, 100.0)
        val now = Instant.now()
        val event = Event(now, listOf(price))

        broker.place(listOf(order))
        broker.sync(event)
        return broker
    }

    @Test
    fun logic() {
        val broker = getFilledSimbroker()
        val account1 = broker.sync()
        val account2 = broker.sync()
        assertEquals(account1.equity(), account2.equity())
    }



    @Test
    fun placeSingleCurrencyOrder() {
        val broker = SimBroker()

        val asset = Asset("TEST")
        val order = MarketOrder(asset, 10)
        val price = TradePrice(asset, 100.0)
        val now = Instant.now()
        val event = Event(now, listOf(price))

        broker.place(listOf(order))
        val account = broker.sync(event)
        assertEquals(1, broker.closedOrders.size)
        assertEquals(0, account.orders.size)
        val orderState = broker.closedOrders.first()

        assertEquals(OrderStatus.COMPLETED, orderState.status)
        assertEquals(asset, orderState.asset)

        assertEquals(1, broker.trades.size)
        val trade = broker.trades.first()
        assertEquals(Size(10), trade.size)
        assertEquals(now, trade.time)

        assertEquals(broker.initialDeposit - trade.totalCost, account.cash)

        assertEquals(1, account.positions.size)
        val pos = account.positions.values.first()
        assertEquals(Size(10), pos.size)
    }

    @Test
    fun placeMultipleOrders() {
        val er = FixedExchangeRates(USD, EUR to 0.8)
        Config.exchangeRates = er
        val broker = SimBroker()
        val event = TestData.event()
        val orders = listOf(TestData.euMarketOrder(), TestData.usMarketOrder())
        broker.place(orders)
        var account = broker.sync(event)
        assertEquals(broker.closedOrders.size, broker.trades.size)
        assertEquals(1, account.orders.size)

        account = broker.sync(TestData.event2())
        assertEquals(1, account.orders.size)
        assertEquals(1, account.positions.values.assets.size)
        assertEquals(1, broker.closedOrders.size)
        assertEquals(1, broker.trades.size)
    }


    @Test
    fun updateOrder() {
        val broker = SimBroker()
        val asset = Asset("TEST")
        val order = LimitOrder(asset,Size.ONE, 99.0)
        val price = TradePrice(asset, 100.0)
        val now = Instant.now()
        val event = Event(now, listOf(price))

        broker.place(listOf(order))
        val account = broker.sync(event)
        assertEquals(1, account.orders.size)
        val state = account.orders.first()

        val order2 = LimitOrder(asset,Size.ONE, 101.0)
        val updateOrder = Modification(state.id, order2)
        val event2 = Event(now + 1.millis, listOf(price))
        broker.place(listOf(updateOrder))
        broker.sync(event2)

    }


}
