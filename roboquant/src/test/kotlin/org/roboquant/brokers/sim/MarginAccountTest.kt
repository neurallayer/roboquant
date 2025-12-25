/*
 * Copyright 2020-2026 Neural Layer
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
import org.roboquant.common.Account
import org.roboquant.brokers.AccountModel
import org.roboquant.brokers.Broker
import org.roboquant.brokers.MarginAccountModel
import org.roboquant.brokers.SimBroker
import org.roboquant.common.*
import org.roboquant.common.Currency.Companion.JPY
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.Event
import org.roboquant.common.TradePrice
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MarginAccountTest {

    @Test
    fun test3() {
        val account = TestData.internalAccount()
        val cc = account.baseCurrency
        val uc = MarginAccountModel()
        uc.updateAccount(account)
        assertTrue(account.buyingPower.value > account.cash[cc])
    }

    @Test
    fun test4() {
        val account = TestData.internalAccount()
        val cc = account.baseCurrency
        val uc = MarginAccountModel(20.0)
        uc.updateAccount(account)
        assertTrue(account.buyingPower.value > account.cash[cc])
    }

    private fun update(broker: Broker, asset: Asset, price: Number, orderSize: Int = 0): Account {
        val p = price.toDouble()
        val orders = if (orderSize == 0) emptyList() else listOf(Order(asset, Size(orderSize), p))
        val item = TradePrice(asset, p)
        val event = Event(Instant.now(), listOf(item))
        broker.placeOrders(orders)
        return broker.sync(event)
    }

    private fun getSimBroker(deposit: Amount, accountModel: AccountModel): SimBroker {
        val wallet = deposit.toWallet()
        return SimBroker(
            wallet,
            accountModel = accountModel
        )
    }

    @Test
    fun testMarginAccountLong() {
        // Slide-2 example in code
        val initial = 1_000_000.JPY
        val broker = getSimBroker(initial, MarginAccountModel())
        val abc = Stock("ABC", JPY)

        var account = update(broker, abc, 1000)
        assertEquals(2_000_000.JPY, account.buyingPower)

        account = update(broker, abc, 1000, 500)
        assertEquals(1_700_000.JPY, account.buyingPower)
        assertEquals(initial, account.equityAmount())

        account = update(broker, abc, 500)
        assertEquals(1_350_000.JPY, account.buyingPower)

        account = update(broker, abc, 500, 2000)
        assertEquals(750_000.JPY, account.buyingPower)

        account = update(broker, abc, 400)
        assertEquals(400_000.JPY, account.buyingPower)

        account = update(broker, abc, 400, -2500)
        assertEquals(1_000_000.JPY, account.buyingPower)

    }

    @Test
    fun testMarginAccountShort() {
        // The example on slide-3 in code
        val initial = 20_000.USD
        val broker = getSimBroker(initial, MarginAccountModel())
        val abc = Stock("ABC")

        var account = update(broker, abc, 200, -50)
        assertEquals(34_000.USD, account.buyingPower)
        assertEquals(initial, account.equityAmount())

        account = update(broker, abc, 300)
        assertEquals(Amount(USD, 21_000), account.buyingPower)

        account = update(broker, abc, 300, -50)
        assertEquals(12_000.USD, account.buyingPower)

        account = update(broker, abc, 300, 100)
        assertEquals(30_000.USD, account.buyingPower)
        assertEquals(15_000.USD.toWallet(), account.cash)
    }

}
