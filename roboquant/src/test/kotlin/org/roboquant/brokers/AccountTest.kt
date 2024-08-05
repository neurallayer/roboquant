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

package org.roboquant.brokers


import org.roboquant.TestData
import org.roboquant.brokers.sim.Trade
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.brokers.sim.timeframe
import org.roboquant.brokers.sim.timeline
import org.roboquant.common.*
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AccountTest {

    @Test
    fun basis() {
        val iAccount = InternalAccount(Currency.USD)
        val account = iAccount.toAccount()
        val amount = account.equityAmount().value
        assertEquals(0.00, amount)
        assertEquals(Amount(Currency.USD, 0.0), account.cashAmount)

        assertEquals(Currency.USD, account.baseCurrency)
        assertTrue(account.positions.isEmpty())

        val e = account.equity()
        assertTrue(e.isEmpty())

        assertEquals(Size.ZERO, account.positionSize(USStock("Dummy")))

    }


    @Test
    fun extensions() {
        val account = TestData.usAccount()
        assertEquals(account.positions.size, account.positions.values.long.size + account.positions.values.short.size)
    }

    @Test
    fun trades() {
        val time = Instant.now()
        val trades = listOf(Trade(time, USStock("ABC"), Size(10), 10.0, 0.0, 0.0, "1"))
        assertEquals(1, trades.timeline.size)
        assertEquals(Timeframe(time, time, true), trades.timeframe)
    }

}
