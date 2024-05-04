/*
 * Copyright 2020-2024 Neural Layer
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
import org.roboquant.common.Timeframe
import org.roboquant.common.seconds
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AlpacaLiveFeedTestIT {

    private val liveTestTime = 30.seconds

    @Test
    fun test() {
        Config.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        feed.subscribeStocks("AAPL")

        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first()
            assertEquals("AAPL", action.second.asset.symbol)
            assertTrue(action.second is PriceBar)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }

    @Test
    fun test2() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        feed.subscribeStocks("AAPL")

        val actions = feed.filter<PriceItem>(Timeframe.next(liveTestTime))
        feed.close()
        if (actions.isNotEmpty()) {
            val action = actions.first().second
            assertTrue(action is PriceBar)
        } else {
            println("No actions found, perhaps exchange is closed")
        }
    }




}
