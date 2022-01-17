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

package org.roboquant.oanda

import org.roboquant.common.TimeFrame
import org.roboquant.common.minutes
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.*

internal class OANDAFeedTest {

    @Test
    fun liveTest() {
        System.getProperty("TEST_OANDA") ?: return
        val feed = OANDALiveFeed()
        feed.subscribeOrderBook("EUR_USD", "USD_JPY", "GBP_USD")
        assertEquals(3, feed.assets.size)
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        feed.close()
        assertTrue(actions.isNotEmpty())
    }

    @Test
    fun historicTest() {
        System.getProperty("TEST_OANDA") ?: return
        val feed = OANDAHistoricFeed()
        val tf = TimeFrame.parse("2020-03-05", "2020-03-06")
        feed.retrieveCandles("EUR_USD", "EUR_GBP", "GBP_USD", timeFrame = tf)
        assertEquals(3, feed.assets.size)

        val tf2 = TimeFrame.parse("2020-03-04", "2020-03-07")
        assertTrue(tf2.contains(feed.timeline.first()))
        assertTrue(tf2.contains(feed.timeline.last()))
        feed.close()
    }
}