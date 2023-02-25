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
import org.roboquant.common.Config
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.GBP
import org.roboquant.common.Currency.Companion.JPY
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.EUR
import org.roboquant.common.USD
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ECBExchangeRatesTest {

    @Test
    fun testECBReferenceRates() {
        val fileName = TestData.dataDir() + "RATES/eurofxref-hist.csv"
        val x = ECBExchangeRates.fromFile(fileName)

        var c = x.getRate(100.EUR, EUR, Instant.now())
        assertEquals(1.0, c)

        c = x.getRate(100.USD, EUR, Instant.now())
        assertTrue(c < 100.0)

        c = x.getRate(100.EUR, JPY, Instant.now())
        assertTrue(c > 100.0)

        val r1 = x.getRate(100.USD, JPY, Instant.MIN)
        val r2 = x.getRate(100.USD, JPY, Instant.MIN.plusMillis(1L))
        assertEquals(r1, r2)

        c = x.getRate(100.USD, JPY, Instant.MAX)
        assertTrue(c > 100.0)

        val currencies = x.currencies
        assertTrue(JPY in currencies)
        assertTrue(EUR in currencies)
        assertTrue(USD in currencies)
        assertTrue(GBP in currencies)
    }

    @Test
    fun load() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val rates = ECBExchangeRates.fromWeb(useCache = true)
        assertTrue(rates.currencies.size > 40)
    }

}
