/*
 * Copyright 2020-2025 Neural Layer
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

import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.USD
import org.roboquant.common.UnsupportedException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NoExchangeRatesTest {

    @Test
    fun basic() {
        val currencyConverter = NoExchangeRates()

        val now = Instant.now()
        val amount1 = 100.USD
        val rate = currencyConverter.getRate(amount1, USD, now)
        assertEquals(1.0, rate)

        assertThrows<UnsupportedException> {
            currencyConverter.convert(0.USD, EUR, now)
        }

        assertThrows<UnsupportedException> {
            currencyConverter.getRate(amount1, EUR, now)
        }
    }

}
