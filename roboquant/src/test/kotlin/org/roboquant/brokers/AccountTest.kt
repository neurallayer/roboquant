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
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import org.roboquant.common.Size
import org.roboquant.common.Stock
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

        assertEquals(Size.ZERO, account.positionSize(Stock("Dummy")))

    }


    @Test
    fun extensions() {
        val account = TestData.usAccount()
        assertEquals(account.positions.size, account.positions.long.size + account.positions.short.size)
    }

}
