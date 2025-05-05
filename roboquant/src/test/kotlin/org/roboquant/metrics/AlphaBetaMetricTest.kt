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

package org.roboquant.metrics

import org.roboquant.TestData
import org.roboquant.common.Position
import org.roboquant.brokers.InternalAccount
import org.roboquant.common.Currency
import org.roboquant.common.Size
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.feeds.toList
import org.roboquant.journals.MemoryJournal
import org.roboquant.strategies.EMACrossover
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

internal class AlphaBetaMetricTest {

    @Test
    fun test() {
        val feed = TestData.feed
        val strategy = EMACrossover.PERIODS_5_15
        val alphaBetaMetric = AlphaBetaMetric(50)
        val logger = MemoryJournal(alphaBetaMetric)
        org.roboquant.run(feed, strategy, journal = logger)

        val alpha = logger.getMetric("account.alpha").last().value
        assertTrue(!alpha.isNaN())

        val beta = logger.getMetric("account.beta").last().value
        assertTrue(!beta.isNaN())
    }

    @Test
    fun test2() {
        val feed = RandomWalk.lastYears(1, nAssets = 5)
        val asset = feed.assets.first()
        val internalAccount = InternalAccount(Currency.USD)
        val metric = AlphaBetaMetric(50)

        val events = feed.toList()
        val startPrice = events.first().prices[asset]!!.getPrice()

        for ((cnt, event) in events.withIndex()) {
            val price = event.prices[asset]!!.getPrice()
            internalAccount.setPosition(asset, Position(Size(100), startPrice, price))
            val account = internalAccount.toAccount()

            val r = metric.calculate(account, event)
            if (cnt < 50) {
                assertTrue(r.isEmpty())
            } else {
                assertContains(r, "account.alpha")
                assertContains(r, "account.beta")

                val alpha = r.getValue("account.alpha")
                val beta = r.getValue("account.beta")
                assertTrue(alpha.isFinite())
                assertTrue(beta.isFinite())
            }

        }

    }

}
