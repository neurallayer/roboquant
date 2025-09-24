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

package org.roboquant.alpaca

import org.roboquant.common.Config
import org.roboquant.common.NewsItems
import org.roboquant.common.Stock
import org.roboquant.common.Timeframe
import org.roboquant.common.seconds
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertTrue

internal class AlpacaMarketNewsLiveFeedTestIT {

    private val liveTestTime = 30.seconds

    /**
     * Run this test to see if you can retrieve live market news. Please
     * note this test may not receive news in that timeframe.
     */
    @Test
    fun testReceiveNewsInTimeframe() {
        Config.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaMarketNewsLiveFeed()
        feed.subscribe(Stock("AAPL"))

        val timeframe = Timeframe.next(liveTestTime)
        val items = feed.filter<NewsItems>(
            timeframe = Timeframe.next(liveTestTime),
            timeOutMillis = timeframe.duration.toMillis()
        )
        feed.close()
        if (items.isNotEmpty()) {
            val newsItems = items.first().second
            val hasAAPL = newsItems.items.firstOrNull()?.assets?.any { it.symbol == "AAPL" } == true
            if (newsItems.items.firstOrNull()?.assets != null) assertTrue(hasAAPL)
            println("At least one news item found for AAPL")
        } else {
            println("No news actions found, perhaps exchange is closed or no news in timeframe")
        }
    }
}
