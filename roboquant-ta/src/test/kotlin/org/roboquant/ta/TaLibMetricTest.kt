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

package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.Currency
import org.roboquant.common.Wallet
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.metrics.MetricResults
import java.time.Instant
import kotlin.test.assertTrue

class TaLibMetricTest {

    @Test
    fun test() {
        val metric = TaLibMetric("ema10", 10) { series ->
            ema(series.close, 10)
        }

        val account = Account(
            Currency.USD,
            Instant.now(),
            Wallet(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            Amount(Currency.USD, 0.0)
        )

        val results = metric.calculate(account, Event(emptyList(), Instant.now()))
        assertTrue(results.isEmpty())

        val feed = HistoricTestFeed(100 until 111, priceBar = true)
        val events = feed.filter<PriceBar>()
        var mResult: MetricResults = emptyMap()
        for (event in events) {
            mResult = metric.calculate(account, Event(listOf(event.second), event.first))
        }
        assertTrue(mResult.isNotEmpty())

        assertDoesNotThrow {
            metric.reset()
        }

    }
}