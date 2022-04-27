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

package org.roboquant.yahoo

import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue


internal class YahooHistoricFeedTestIT {

    @Test
    fun test() {
        System.getenv("TEST_YAHOO") ?: return
        val feed = YahooHistoricFeed()
        val asset = Asset("AAPL")
        feed.retrieve(listOf(asset), timeframe = Timeframe.fromYears(2018, 2020))
        assertTrue(feed.assets.first().symbol == "AAPL")

        feed.retrieve("GOOGL", timeframe = Timeframe.fromYears(2019, 2020))
        assertContains(feed.assets.map { it.symbol }, "GOOGL")
    }


}