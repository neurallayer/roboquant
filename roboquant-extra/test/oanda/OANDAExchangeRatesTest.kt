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

package org.roboquant.oanda

import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.JPY
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.EUR
import org.roboquant.common.USD
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OANDAExchangeRatesTest {

    @Test
    fun test() {
        System.getProperty("TEST_OANDA") ?: return
        val c = OANDAExchangeRates.allAvailableAssets()
        var r = c.getRate(100.USD, EUR, Instant.now())
        assertTrue(r.isFinite())

        c.refresh()
        r = c.getRate(100.USD, EUR, Instant.now())
        assertTrue(r.isFinite())
    }


    @Test
    fun test2() {
        System.getProperty("TEST_OANDA") ?: return
        val c = OANDAExchangeRates.allAvailableAssets()
        val a = c.convert(100.USD, JPY, Instant.now())
        assertTrue(a > 1000)

        val r = c.getRate(100.USD, USD, Instant.now())
        assertEquals(1.0, r)

        val r2 = c.getRate(100.EUR, JPY, Instant.now())
        assertTrue(r2 > 50)
    }
}