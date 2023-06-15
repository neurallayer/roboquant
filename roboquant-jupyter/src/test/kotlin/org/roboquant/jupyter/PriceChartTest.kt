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

package org.roboquant.jupyter

import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Action
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.Indicator
import org.roboquant.strategies.EMAStrategy
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class PriceChartTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(1)
        val asset = feed.assets.first()
        Chart.counter = 0
        val chart = PriceChart(feed, asset)
        val html = chart.asHTML()
        assertTrue(html.isNotBlank())

        Chart.counter = 0
        val chart2 = PriceChart(feed, asset.symbol)
        assertEquals(html, chart2.asHTML())

        Chart.counter = 0
        val chart3 = PriceChart(feed, asset.symbol, priceType = "OPEN")
        assertNotEquals(html, chart3.asHTML())
    }

    @Test
    fun trades() {
        val tf = Timeframe.fromYears(2020, 2021)
        val feed = RandomWalkFeed(tf)
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, logger = SilentLogger())
        rq.run(feed)

        val trades = rq.broker.account.trades
        assertTrue(trades.isNotEmpty())
        val asset = trades.first().asset
        Chart.counter = 0
        val chart = PriceChart(feed, asset, trades = trades)
        assertTrue(chart.asHTML().isNotEmpty())
    }

    @Test
    fun indicators() {
        class MyIndicator : Indicator {
            override fun calculate(action: Action, time: Instant): Map<String, Double> {
                return mapOf("one" to 1.0, "two" to 2.0)
            }
        }

        val tf = Timeframe.fromYears(2020, 2021)
        val feed = RandomWalkFeed(tf)
        val asset = feed.assets.first()
        val ind = MyIndicator()
        Chart.counter = 0

        val chart = PriceChart(feed, asset, indicators = arrayOf(ind))
        assertTrue(chart.asHTML().isNotEmpty())
    }

}