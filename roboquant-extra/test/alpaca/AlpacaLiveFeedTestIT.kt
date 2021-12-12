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

package org.roboquant.alpaca

import org.roboquant.common.TimeFrame
import org.roboquant.common.days
import org.roboquant.common.getBySymbol
import org.roboquant.common.minutes
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertTrue


internal class AlpacaLiveFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        val assets = feed.availableAssets
        val apple = assets.getBySymbol("AAPL")
        feed.subscribe(apple)
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.disconnect()
    }


    @Test
    fun test2() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        feed.subscribe("AAPL")
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.disconnect()
    }

    @Test
    fun testHistoric() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.isNotEmpty())
        feed.retrieve("AAPL", timeFrame = TimeFrame.past(100.days))
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())
    }

    @Test
    fun test3() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed(autoConnect = false)
        feed.connect()
        feed.subscribeAll()
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.disconnect()
    }

}