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
import org.roboquant.TestData
import org.roboquant.common.Currency
import org.roboquant.common.Wallet
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class AccountTest {

    @Test
    fun basis() {
        val iAccount = InternalAccount()
        val account = iAccount.toAccount()
        val amount = account.equityAmount.value
        assertEquals(0.00, amount)
        assertEquals(Currency.USD, account.baseCurrency)
        assertTrue(account.portfolio.isEmpty())

        val e = account.equity
        assertEquals(Wallet(), e)

        assertTrue(account.trades.isEmpty())
        assertTrue(account.trades.realizedPNL.isEmpty())
    }

    @Test
    fun testValues() {
        val account = TestData.internalAccount().toAccount()
        assertTrue(account.closedOrders.isNotEmpty())
        assertContains(account.getOrderTrades(), account.closedOrders.first())
    }

    @Test
    fun testIntervalValues() {
        val account = TestData.internalAccount()
        assertTrue(account.closedOrders.isNotEmpty())
        assertTrue(account.trades.isEmpty())
    }

    @Test
    fun summaries() {
        val account = TestData.usAccount()
        assertTrue(account.portfolio.summary().content.isNotEmpty())
        assertTrue(account.closedOrders.summary().content.isNotEmpty())
        assertTrue(account.trades.summary().content.isNotEmpty())
        assertTrue(account.cash.summary().content.isNotEmpty())

        assertTrue(account.summary().content.isNotEmpty())
        assertTrue(account.fullSummary().content.isNotEmpty())
    }

    @Test
    fun extensions() {
        val account = TestData.usAccount()
        assertEquals(account.portfolio.size, account.portfolio.long.size + account.portfolio.short.size)
        assertContains(account.portfolio.assets, account.portfolio.first().asset)

    }




}