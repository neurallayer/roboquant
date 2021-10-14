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


import org.junit.Test
import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Asset
import org.roboquant.common.Currency.Companion.EUR
import org.roboquant.common.Currency.Companion.USD
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.policies.TestPolicy
import org.roboquant.strategies.EMACrossover
import kotlin.test.assertEquals


internal class FixedExchangeRatesTest {

    @Test
    fun multiCurrency() {
        val feed = CSVFeed(TestData.dataDir() + "US")
        val asset = Asset("TEMPLATE", currencyCode = "EUR")
        val config = CSVConfig(template = asset)
        val feed2 = CSVFeed(TestData.dataDir() +"EU", config)
        feed.merge(feed2)

        val currencyConverter = FixedExchangeRates(USD, EUR to 1.2)
        assertEquals(USD, currencyConverter.baseCurrency)

        val broker = SimBroker(currencyConverter = currencyConverter)

        val strategy = EMACrossover()
        val policy = TestPolicy()
        val roboquant = Roboquant(
            strategy,
            AccountSummary(),
            policy = policy,
            broker = broker,
            logger = SilentLogger()
        )
        roboquant.run(feed)

        assertEquals(2, broker.account.cash.currencies.size)
    }


}