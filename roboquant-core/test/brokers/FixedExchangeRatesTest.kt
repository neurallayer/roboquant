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


import org.junit.jupiter.api.Test
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.USD
import java.time.Instant
import kotlin.test.assertEquals


internal class FixedExchangeRatesTest {

    @Test
    fun basic() {
        val currencyConverter = FixedExchangeRates(USD, EUR to 1.2)
        assertEquals(USD, currencyConverter.baseCurrency)

        val now = Instant.now()
        val amount1 = 100.USD
        val r1 = currencyConverter.getRate(amount1, USD, now)
        assertEquals(1.0, r1)

        val r2 = currencyConverter.getRate(amount1, EUR, now)
        assertEquals(1.0/1.2, r2)
    }


}