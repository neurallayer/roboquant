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

package org.roboquant.samples

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.tiingo.TiingoHistoricFeed
import org.roboquant.tiingo.TiingoLiveFeed
import java.time.Instant
import kotlin.test.Ignore
import kotlin.test.Test

internal class TiingoSamples {

    @Test
    @Ignore
    internal fun testLiveFeed() {
        System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        val feed = TiingoLiveFeed.iex()
        feed.subscribe("AAPL", "TSLA")
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        val account = rq.run(feed, Timeframe.next(10.minutes))
        println(account.fullSummary())
    }

    @Test
    @Ignore
    internal fun aggregatorLiveFeed() {
        val iex = TiingoLiveFeed.iex()
        iex.subscribe()
        val feed = AggregatorLiveFeed(iex, 5.seconds)
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        val account = rq.run(feed, Timeframe.next(5.minutes))
        println(account.fullSummary())
    }

    @Test
    @Ignore
    internal fun testLiveFeedFX() {
        val feed = TiingoLiveFeed.fx()
        feed.subscribe("EURUSD")
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        val account = rq.run(feed, Timeframe.next(1.minutes))
        println(account.fullSummary())
    }

    @Test
    @Ignore
    internal fun testLiveFeedCrypto() {
        val feed = TiingoLiveFeed.crypto()
        val asset = Asset("BNB/FDUSD", AssetType.CRYPTO, "FDUSD")
        Config.registerAsset("BNBFDUSD", asset)

        feed.subscribe("BNBFDUSD")
        val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
        val account = rq.run(feed, Timeframe.next(1.minutes))
        println(account.fullSummary())
    }


    @Test
    @Ignore
    internal fun testLiveFeedMeasureDelay() {
        val feed = TiingoLiveFeed.crypto()
        feed.subscribe() // subscribe to all crypto currencies
        var n = 0
        var sum = 0L
        feed.apply<PriceAction>(Timeframe.next(1.minutes)) { _, time ->
            val now = Instant.now()
            sum += now.toEpochMilli() - time.toEpochMilli()
            n++
        }
        feed.close()
        println("average delay is ${sum/n}ms")
    }


    private fun Feed.measure(
        timeframe: Timeframe,
    ) = runBlocking {
        // We need a channel with enough capacity
        val channel = EventChannel(10_000, timeframe = timeframe)
        playBackground(channel)
        var sum = 0L
        var n = 0L

        try {
            while (true) {
                val o = channel.receive()
                if (o.actions.isNotEmpty()) {
                    sum += System.currentTimeMillis() - o.time.toEpochMilli()
                    n++
                }
            }

        } catch (_: ClosedReceiveChannelException) {
            // Intentionally left empty
        }

        println("average delay=${sum/n}ms events=$n")

    }


    @Test
    @Ignore
    internal fun testLiveFeedMeasure() {
        // This will retrieve a lot of data
        val feed = TiingoLiveFeed.iex(thresholdLevel = 0)
        feed.subscribe() // subscribe to all IEX quotes and trades
        feed.measure(Timeframe.next(10.minutes))
        feed.close()
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
        val account = rq.run(feed)
        println(account.fullSummary())
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
        val account = rq.run(feed)
        println(account.fullSummary())
    }



}
