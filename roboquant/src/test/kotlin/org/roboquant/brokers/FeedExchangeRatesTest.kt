/*
 * Copyright 2020-2022 Neural Layer
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
import org.junit.jupiter.api.assertThrows
import org.roboquant.common.Asset
import org.roboquant.common.CAD
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.common.USD
import org.roboquant.feeds.test.HistoricTestFeed
import java.time.Instant
import kotlin.test.assertEquals

internal class FeedExchangeRatesTest {

    @Test
    fun basic() {
        val rates = listOf(1.15, 1.16, 1.20, 1.18)
        val feed = HistoricTestFeed(rates, asset = Asset.forexPair("USD_EUR"))
        val er = FeedExchangeRates(feed)
        assertEquals(setOf(EUR, USD), er.currencies)
        val r = er.convert(100.USD, EUR, Instant.now())
        assertEquals(EUR, r.currency)
        assertEquals(118.0, r.value)

        assertThrows<ConfigurationException> {
            er.convert(100.CAD, EUR, Instant.now())
        }
    }

}