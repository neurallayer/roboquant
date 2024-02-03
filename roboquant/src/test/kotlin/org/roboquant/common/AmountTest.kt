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

package org.roboquant.common

import kotlin.test.Test
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import kotlin.test.*

internal class AmountTest {

    @Test
    fun testBasic() {
        val a1 = 100.EUR
        val a2 = Amount(EUR, 100.0)
        assertEquals(a1, a2)

        val a3 = a2 * 2
        assertEquals(a2.value * 2, a3.value)

        val a5 = Amount("EUR", 100)
        assertEquals(a1, a5)
    }

    @Test
    fun calc() {
        var a = 100.EUR + 2
        assertEquals(102.0, a.value)

        a *= 2
        assertEquals(204.0, a.value)

        a /= 2
        assertEquals(102.0, a.value)

        a -= 2
        assertEquals(100.0, a.value)

        assertTrue(a.isPositive)

    }

    @Test
    fun test() {
        val c1 = 100.EUR + 200.USD
        val c2 = 200.USD + 100.EUR
        val c3 = listOf(100.EUR, 200.USD).toWallet()
        assertEquals(c1, c2)
        assertEquals(c1, c3)

        assertEquals(200.USD, c2.getAmount(USD))

        val c4 = 201.USD + 100.EUR
        assertNotEquals(c4, c2)

        assertEquals(10.EUR, 10.0.EUR)

    }

    @Test
    fun testPredefined() {
        val wallet = 1.EUR + 1.USD + 1.JPY + 1.GBP + 1.CHF + 1.AUD + 1.CAD +
                1.CNY + 1.HKD + 1.NZD + 1.RUB + 1.INR + 1.BTC + 1.ETH + 1.USDT
        assertEquals(15, wallet.currencies.size)
    }

    @Test
    fun testFormat() {
        val c1 = 100.EUR
        val f = c1.formatValue(4)
        assertEquals("100.0000", f)
        assertEquals("EUR 100.00", c1.toString())

        val c2 = 10000.USD
        val f2 = c2.toString()
        assertEquals("USD 10,000.00", f2)
    }

}
