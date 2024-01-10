/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.tiingo

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.latestRun
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertTrue

internal class TiingoTestIT {

    @Test
    internal fun testLiveFeed() {
        Config.getProperty("TEST_TIINGO") ?: return
        val feed = TiingoLiveFeed.iex()
        feed.subscribe("AAPL", "TSLA")
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = LastEntryLogger())
        rq.run(feed, Timeframe.next(1.minutes))
        val actions = rq.logger.getMetric("progress.actions").latestRun()
        assertTrue(actions.last().value > 0)
    }

    @Test
    internal fun testLiveFeedFX() {
        Config.getProperty("TEST_TIINGO") ?: return
        val feed = TiingoLiveFeed.fx()
        feed.subscribe("EURUSD")
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = LastEntryLogger())
        rq.run(feed, Timeframe.next(1.minutes))
        val actions = rq.logger.getMetric("progress.actions").latestRun()
        assertTrue(actions.last().value > 0)
    }

    @Test
    internal fun testLiveFeedCrypto() {
        Config.getProperty("TEST_TIINGO") ?: return
        val feed = TiingoLiveFeed.crypto()
        val asset = Asset("BNBFDUSD", AssetType.CRYPTO, "FDUSD")
        feed.subscribeAssets(asset)
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = LastEntryLogger())
        rq.run(feed, Timeframe.next(1.minutes))
        val actions = rq.logger.getMetric("progress.actions").latestRun()
        assertTrue(actions.last().value > 0)
    }


}
