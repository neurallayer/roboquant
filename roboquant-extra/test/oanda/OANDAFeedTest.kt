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

import org.roboquant.common.Timeframe
import org.roboquant.common.seconds
import org.roboquant.feeds.OrderBook
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OANDAFeedTest {

    private val liveTestTime = 30.seconds
    private val symbols = arrayListOf("EUR_USD", "USD_JPY", "GBP_USD").toTypedArray()

    @Test
    fun liveTest() {
        System.getenv("TEST_OANDA") ?: return
        val feed = OANDALiveFeed()
        feed.subscribeOrderBook(*symbols)
        assertEquals(3, feed.assets.size)
        val actions = feed.filter<PriceAction>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first()
            assertContains(symbols, action.second.asset.symbol)
            assertTrue(action.second is OrderBook)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }

    @Test
    fun historicTest() {
        System.getenv("TEST_OANDA") ?: return
        val feed = OANDAHistoricFeed()
        val tf = Timeframe.parse("2020-03-05", "2020-03-06")
        feed.retrieveCandles(*symbols, timeframe = tf)
        assertEquals(3, feed.assets.size)

        val tf2 = Timeframe.parse("2020-03-04", "2020-03-07")
        assertTrue(tf2.contains(feed.timeline.first()))
        assertTrue(tf2.contains(feed.timeline.last()))
        feed.close()
    }
}