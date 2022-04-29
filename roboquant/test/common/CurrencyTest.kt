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

package org.roboquant.common

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class CurrencyTest {

    @Test
    fun test() {
        assertTrue {
            Currency.EUR
            Currency.USD
            Currency.USD
            Currency.CAD
            Currency.CHF
            Currency.JPY
            Currency.HKD
            Currency.GBP
            Currency.AUD
            Currency.CNY
            Currency.NZD
            Currency.RUB
            Currency.INR
            Currency.BTC
            Currency.ETH
            Currency.USDT
            true
        }
    }

    @Test
    fun test2() {
        val x = Currency.USD
        val y = Currency.getInstance("USD")
        assertEquals(x, y)
    }


    @Test
    fun test3() {
        val c = Currency.getInstance("DUMMY")
        assertEquals(2, c.defaultFractionDigits)
        Currency.increaseDigits(2)
        assertEquals(4, c.defaultFractionDigits)
        Currency.increaseDigits(-2)
        assertEquals(2, c.defaultFractionDigits)
    }


}