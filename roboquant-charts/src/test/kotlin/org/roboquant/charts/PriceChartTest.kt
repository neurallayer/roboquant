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

package org.roboquant.charts

import org.roboquant.common.Timeframe
import org.roboquant.common.Item
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.ta.Indicator
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class PriceChartTest {

    @Test
    fun test() {
        val feed = RandomWalk.lastYears(1)
        val asset = feed.assets.first()
        val chart = PriceChart(feed, asset)
        val html = chart.renderJson()
        assertTrue(html.isNotBlank())

        val chart2 = PriceChart(feed, asset.symbol)
        assertEquals(html, chart2.renderJson())
        val chart3 = PriceChart(feed, asset.symbol, priceType = "OPEN")
        assertNotEquals(html, chart3.renderJson())
    }


    @Test
    fun indicators() {
        class MyIndicator : Indicator {
            override fun calculate(item: Item, time: Instant): Map<String, Double> {
                return mapOf("one" to 1.0, "two" to 2.0)
            }

            override fun clear() {
                // NOP
            }
        }

        val tf = Timeframe.fromYears(2020, 2021)
        val feed = RandomWalk(tf)
        val asset = feed.assets.first()
        val ind = MyIndicator()

        val chart = PriceChart(feed, asset, indicators = arrayOf(ind))
        assertTrue(chart.renderJson().isNotEmpty())
    }

}
