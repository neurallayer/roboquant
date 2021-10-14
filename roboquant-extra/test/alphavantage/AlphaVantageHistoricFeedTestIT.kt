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

package org.roboquant.alphavantage

import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.junit.Test

internal class AlphaVantageHistoricFeedTestIT {

    @Test
    fun alphaVantage() {
        System.getProperty("TEST_ALPHAVANTAGE") ?: return
        val strategy = EMACrossover.shortTerm()
        val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), logger = SilentLogger())

        val feed = AlphaVantageHistoricFeed("dummy", compensateTimeZone = true, generateSinglePrice = false)
        val asset = Asset("AAPL", AssetType.STOCK,"USD", "NASDAQ")
        feed.retrieve(asset)
        roboquant.run(feed)
    }


}