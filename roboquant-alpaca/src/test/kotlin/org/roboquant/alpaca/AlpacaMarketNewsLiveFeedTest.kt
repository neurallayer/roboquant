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

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.roboquant.common.NewsItems
import org.roboquant.common.Stock
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.filter
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AlpacaMarketNewsLiveFeedTest {

    /*
     * Test that the live feed can retrieve news items.
     * This test doesn't need a real API key as it mocks the Alpaca API.
     */
    @Test
    fun testMockReceiveNews() = runBlocking {
        // Do NOT connect to Alpaca
        val feed = AlpacaMarketNewsLiveFeed(autoConnect = false)
        feed.subscribe(Stock("AAPL"))

        // Emit two news items within the timeframe
        val job = launch {
            val t0 = Instant.now()
            val n1 = NewsItems.NewsItem(
                id = "n1",
                assets = listOf(Stock("AAPL")),
                content = "First test news",
                headline = "AAPL News 1",
                url = null,
                meta = mapOf("source" to "test")
            )
            feed.sendNews(t0, NewsItems(listOf(n1)))

            delay(50)

            val n2 = NewsItems.NewsItem(
                id = "n2",
                assets = listOf(Stock("AAPL")),
                content = "Second test news",
                headline = "AAPL News 2",
                url = null,
                meta = mapOf("source" to "test")
            )
            feed.sendNews(t0.plusMillis(25), NewsItems(listOf(n2)))
        }

        val timeframe = Timeframe.next(TimeSpan(seconds = 1))
        val received = feed.filter<NewsItems>(timeframe)
        job.cancel()
        feed.close()

        // Verify we got exactly two news items in the timeframe
        assertEquals(2, received.size, "Expected to receive exactly two NewsItems events")

        // And both should reference AAPL in their assets list
        received.forEach { pair ->
            val newsItems = pair.second
            val hasAAPL = newsItems.items.firstOrNull()?.assets?.any { it.symbol == "AAPL" } == true
            assertEquals(true, hasAAPL)
        }
    }
}
