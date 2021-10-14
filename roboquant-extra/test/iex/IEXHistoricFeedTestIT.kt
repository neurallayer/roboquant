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

package org.roboquant.iex

import org.junit.Test
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertTrue

internal class IEXHistoricFeedTestIT {

    @Test
    fun test() {
        System.getProperty("TEST_IEX") ?: return
        val token = Config.getProperty("IEX_TOKEN")!!
        val feed = IEXHistoricFeed(token)
        val asset = Asset("AAPL")
        feed.retrieveIntraday(asset)
        assertTrue(asset in feed.assets)

        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
    }

    @Test
    fun testPriceBar() {
        System.getProperty("IEX_TEST") ?: return
        val token = Config.getProperty("IEX_TOKEN")!!
        val feed = IEXHistoricFeed(token)
        val asset = Asset("AAPL")
        feed.retrievePriceBar(asset)
        Thread.sleep(2000)
        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
    }

    @Test
    fun testIntraday() {
        System.getProperty("IEX_TEST") ?: return
        val token = Config.getProperty("IEX_TOKEN")!!
        val feed = IEXHistoricFeed(token)
        feed.retrieveIntraday("AAPL")
        val actions = feed.filter<PriceAction>(TimeFrame.nextMinutes(5))
        assertTrue(actions.isNotEmpty())
    }

}