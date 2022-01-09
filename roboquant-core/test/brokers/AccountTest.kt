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

package org.roboquant.brokers


import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.common.Wallet
import org.roboquant.common.Currency
import org.roboquant.common.USD
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AccountTest {

    @Test
    fun basis() {
        val account = Account()
        val amount = account.equityAmount.value
        assertEquals(0.00, amount)
        assertEquals(Currency.getInstance("USD"), account.baseCurrency)
        assertTrue(account.portfolio.isEmpty())

        val e = account.equity
        assertEquals(Wallet(), e)

        assertTrue(account.trades.isEmpty())
        assertTrue(account.trades.realizedPnL().isEmpty())

        val asset = Asset("Dummy")
        assertTrue(account.trades.realizedPnL(asset).value == 0.0)

        account.cash.deposit(100.USD)

        val s = account.summary()
        assertTrue(s.toString().isNotEmpty())

        val s2 = account.fullSummary()
        assertTrue(s2.toString().isNotEmpty())

        val o = account.orders
        assertTrue(o.summary().toString().isNotEmpty())
    }


}