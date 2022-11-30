/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.iexcloud

import org.junit.jupiter.api.Test
import org.roboquant.common.symbols
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IEXCloudHistoricFeedTestIT {

    @Test
    fun testIntraday() {
        System.getProperty("FULL_COVERAGE") ?: return
        val feed = IEXCloudHistoricFeed()
        feed.retrieveIntraday("AAPL")
        assertTrue("AAPL" in feed.assets.symbols)
        assertFalse("IBM" in feed.assets.symbols)

        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())
    }

    @Test
    fun testPriceBar() {
        System.getProperty("TEST_IEX") ?: return
        val feed = IEXCloudHistoricFeed()
        feed.retrieve("AAPL")
        Thread.sleep(2000)
        val actions = feed.filter<PriceAction>()
        assertTrue(actions.isNotEmpty())
        assertEquals("AAPL", actions.first().second.asset.symbol)
        assertFalse { actions.any { it.second.asset.symbol != "AAPL" } }
    }

    
}