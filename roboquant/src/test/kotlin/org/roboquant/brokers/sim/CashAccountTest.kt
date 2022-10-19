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

package org.roboquant.brokers.sim

import org.roboquant.TestData
import org.roboquant.brokers.Account
import org.roboquant.brokers.Broker
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.marketValue
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.EUR
import org.roboquant.feeds.Event
import org.roboquant.feeds.TradePrice
import org.roboquant.orders.MarketOrder
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CashAccountTest {

    /**
     * @suppress
     */
    internal companion object {

        internal fun update(broker: Broker, asset: Asset, price: Number, orderSize: Int = 0): Account {
            val orders = if (orderSize == 0) emptyList() else listOf(MarketOrder(asset, orderSize))
            val action = TradePrice(asset, price.toDouble())
            val event = Event(listOf(action), Instant.now())
            return broker.place(orders, event)
        }

        internal fun getSimBroker(deposit: Amount, accountModel: AccountModel): SimBroker {
            val wallet = deposit.toWallet()
            return SimBroker(
                wallet,
                accountModel = accountModel,
                pricingEngine = NoCostPricingEngine(),
                feeModel = NoFeeModel()
            )
        }
    }

    @Test
    fun test() {
        val account = TestData.internalAccount()
        val uc = CashAccount()
        val result = uc.getBuyingPower(account)
        assertEquals(result, account.cash.getAmount(result.currency))

        val order = MarketOrder(TestData.usStock(), 100)
        val state = OrderState(order, OrderStatus.ACCEPTED, Instant.now())
        account.putOrders(listOf(state))
        val result2 = uc.getBuyingPower(account)

        // Right now open orders are not taken into account
        assertEquals(result2.value, result.value)
    }

    @Test
    fun testCashAccount() {
        // Slide 1 example in code
        val initial = 10_000.EUR
        val broker = getSimBroker(initial, accountModel = CashAccount())
        val abc = Asset("ABC", currencyCode = "EUR")
        val xyz = Asset("XYZ", currencyCode = "USD")

        Config.exchangeRates = FixedExchangeRates(EUR, USD to 0.9)

        var account = update(broker, abc, 100)
        assertEquals(10_000.EUR, account.buyingPower)

        account = update(broker, abc, 100, 40)
        assertEquals(initial, account.equityAmount)
        assertEquals(4_000.EUR.toWallet(), account.positions.marketValue)
        assertEquals(6_000.EUR, account.buyingPower)

        account = update(broker, abc, 75)
        assertEquals(9_000.EUR, account.equityAmount)

        account = update(broker, abc, 75, -40)
        assertEquals(9_000.EUR, account.buyingPower)

        account = update(broker, xyz, 200, 25)
        assertEquals(4_500.EUR, account.buyingPower)

        account = update(broker, xyz, 240)
        assertEquals(4_500.EUR, account.buyingPower)

        account = update(broker, xyz, 240, -25)
        assertEquals(9900.EUR, account.buyingPower)

    }

}