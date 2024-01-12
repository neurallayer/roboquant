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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.AggregatorLiveFeed
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.tiingo.TiingoHistoricFeed
import org.roboquant.tiingo.TiingoLiveFeed
import kotlin.test.Ignore
import kotlin.test.Test

internal class TiingoSamples {

    @Test
    @Ignore
    internal fun testLiveFeed() {
        val feed = TiingoLiveFeed.iex()
        feed.subscribe("AAPL", "TSLA")
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        rq.run(feed, Timeframe.next(10.minutes))
        println(rq.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun aggregatorLiveFeed() {
        val iex = TiingoLiveFeed.iex()
        iex.subscribe()
        val feed = AggregatorLiveFeed(iex, 5.seconds)
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = MemoryLogger())
        rq.run(feed, Timeframe.next(3.minutes))
        println(rq.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun testLiveFeedFX() {
        val feed = TiingoLiveFeed.fx()
        feed.subscribe("EURUSD")
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        rq.run(feed, Timeframe.next(1.minutes))
        println(rq.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun testLiveFeedCrypto() {
        val feed = TiingoLiveFeed.crypto()
        val asset = Asset("BNBFDUSD", AssetType.CRYPTO, "FDUSD")
        feed.subscribeAssets(asset)
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        rq.run(feed, Timeframe.next(1.minutes))
        println(rq.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun historic() {
        val feed = TiingoHistoricFeed()
        val tf = Timeframe.past(3.years)
        feed.retrieve("AAPL", "TSLA", "ERROR_SYMBOL", timeframe=tf)
        println(feed.assets)
        println(feed.timeframe)
        val rq = Roboquant(EMAStrategy(), ProgressMetric())
        rq.run(feed)
        println(rq.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun historicIntraDay() {
        val feed = TiingoHistoricFeed()
        val tf = Timeframe.past(10.days)
        feed.retrieveIntraday("AAPL", "TSLA", "ERROR_SYMBOL", timeframe=tf, frequency = "1hour")
        println(feed.assets)
        println(feed.timeframe)
        val rq = Roboquant(EMAStrategy(), ProgressMetric())
        rq.run(feed)
        println(rq.broker.account.fullSummary())
    }



}
