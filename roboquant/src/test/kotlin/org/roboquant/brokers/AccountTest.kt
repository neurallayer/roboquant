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

package org.roboquant.brokers


import org.junit.jupiter.api.Test
import org.roboquant.TestData
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.orders.summary
import java.time.Instant
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AccountTest {

    @Test
    fun basis() {
        val iAccount = InternalAccount(Currency.USD)
        val account = iAccount.toAccount()
        val amount = account.equityAmount.value
        assertEquals(0.00, amount)
        assertEquals(Amount(Currency.USD, 0.0), account.cashAmount)

        assertEquals(Currency.USD, account.baseCurrency)
        assertTrue(account.positions.isEmpty())

        val e = account.equity
        assertTrue(e.isEmpty())

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
        val account = TestData.internalAccount().toAccount()
        assertTrue(account.closedOrders.isNotEmpty())
        assertTrue(account.trades.isEmpty())
    }

    @Test
    fun summaries() {
        val account = TestData.usAccount()
        assertTrue(account.positions.summary().content.isNotEmpty())
        assertTrue(account.closedOrders.summary().content.isNotEmpty())
        assertTrue(account.trades.summary().content.isNotEmpty())
        assertTrue(account.cash.summary().content.isNotEmpty())

        assertTrue(account.summary().content.isNotEmpty())
        assertTrue(account.fullSummary().content.isNotEmpty())
        assertTrue(account.fullSummary(true).content.isNotEmpty())
    }

    @Test
    fun extensions() {
        val account = TestData.usAccount()
        assertEquals(account.positions.size, account.positions.long.size + account.positions.short.size)
    }

    @Test
    fun trades() {
        val time = Instant.now()
        val trades = listOf(Trade(time, Asset("ABC"), Size(10), 10.0, 0.0, 0.0, 1))
        assertEquals(1, trades.timeline.size)
        assertEquals(Timeframe(time, time, true), trades.timeframe)
    }

}