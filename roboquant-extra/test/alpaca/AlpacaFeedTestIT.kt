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


internal class AlpacaFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        val assets = feed.availableAssets
        val apple = assets.getBySymbol("AAPL")
        feed.subscribe(apple)
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.close()
    }


    @Test
    fun test2() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed()
        feed.subscribe("AAPL")
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.close()
    }

    @Test
    fun testHistoric() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaHistoricFeed()
        val assets = feed.availableAssets
        assertTrue(assets.isNotEmpty())
        val tf = TimeFrame.past(10.days)
        feed.retrieve("AAPL", timeFrame = tf)
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())

        val tf2 = tf.extend(1.days)
        assertTrue(tf2.contains(feed.timeline.first()))
        assertTrue(tf2.contains(feed.timeline.last()))
    }

    @Test
    fun test3() {
        System.getProperty("TEST_ALPACA") ?: return
        val feed = AlpacaLiveFeed(autoConnect = false)
        feed.connect()
        feed.subscribeAll()
        val actions = feed.filter<PriceAction>(TimeFrame.next(5.minutes))
        assertTrue(actions.isNotEmpty())
        feed.close()
    }

}