/*
 * Copyright 2020-2023 Neural Layer
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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.Roboquant
import org.roboquant.binance.BinanceHistoricFeed
import org.roboquant.binance.BinanceLiveFeed
import org.roboquant.binance.Interval
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.AvroFeed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.toList
import org.roboquant.http.WebServer
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.xchange.XChangePollingLiveFeed
import kotlin.test.assertEquals

fun recordBinanceFeed() {
    val feed = BinanceHistoricFeed()
    println(feed.availableAssets.summary())
    val twoYears = Timeframe.parse("2020-01-01", "2022-10-01")
    for (period in twoYears.split(12.hours)) {
        feed.retrieve("BTCUST", "ETHUST", timeframe = period, interval = Interval.ONE_MINUTE)
        println("$period ${feed.timeline.size}")
        Thread.sleep(10) // Sleep a bit to avoid hitting API limitations
    }

    // Now store as Avro file
    val userHomeDir = System.getProperty("user.home")
    val fileName = "$userHomeDir/tmp/crypto_2years.avro"
    AvroFeed.record(feed, fileName)

    // Some sanity checks if we stored what we captured
    val feed2 = AvroFeed(fileName)
    require(feed2.assets.size == feed.assets.size)
    println("done")
}

fun useBinanceFeed() {
    val userHomeDir = System.getProperty("user.home")
    val fileName = userHomeDir / "tmp" / "crypto_2years.avro"
    val feed = AvroFeed(fileName)

    val initialDeposit = Amount("UST", 100_000).toWallet()
    val marginAccount = MarginAccount()
    val policy = FlexPolicy(shorting = true, fractions = 4)
    val broker = SimBroker(initialDeposit, accountModel = marginAccount)
    val roboquant = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), broker = broker, policy = policy)
    roboquant.run(feed)
    println(roboquant.broker.account.summary())
}


fun binanceLiveFeed() {
    val feed = BinanceLiveFeed()
    feed.subscribePriceBar("BTCBUSD", "ETHBUSD", interval = Interval.ONE_MINUTE)
    val events = feed.toList(Timeframe.next(10.minutes)).filter { it.actions.isNotEmpty() }
    println(events.size)
}


fun binanceForwardTest() {
    val feed = BinanceLiveFeed()

    // We ony trade Bitcoin/BUSD
    // feed.subscribePriceQuote("BTCBUSD")
    feed.subscribePriceBar("BTCBUSD")
    val strategy = EMAStrategy.PERIODS_5_15
    val initialDeposit = Amount("BUSD", 10_000).toWallet()
    val broker = SimBroker(initialDeposit)
    val policy = FlexPolicy.singleAsset(enableMetrics = true)
    val rq = Roboquant(strategy, broker = broker, policy = policy, logger = ConsoleLogger())

    // We'll run the forward test for thirty minutes
    val tf = Timeframe.next(30.minutes)
    rq.run(feed, tf)
}


suspend fun binanceWebServer() {
    val feed = BinanceLiveFeed()
    feed.subscribePriceBar("BTCBUSD")
    val strategy = EMAStrategy.PERIODS_5_15
    val initialDeposit = Amount("BUSD", 10_000).toWallet()
    val broker = SimBroker(initialDeposit)
    val policy = FlexPolicy.singleAsset(enableMetrics = true)
    val rq = Roboquant(strategy, broker = broker, policy = policy, logger = ConsoleLogger())

    val server = WebServer(username= "test", password="secret")
    server.start()
    val tf = Timeframe.next(8.hours)
    server.runAsync(rq, feed, tf)
    server.stop()
}

fun binanceBackTest() {
    val strategy = EMAStrategy()
    val initialDeposit = Amount("BUSD", 100_000).toWallet()
    val roboquant = Roboquant(strategy, ScorecardMetric(), broker = SimBroker(initialDeposit))

    val feed = BinanceHistoricFeed()
    val threeYears = Timeframe.parse("2020-01-01", "2023-01-01")
    feed.retrieve("BTCBUSD", "ETHBUSD", timeframe = threeYears, interval = Interval.DAILY)

    roboquant.run(feed)
    println(roboquant.broker.account.summary())
}


fun xchangeFeed() {
    val exchange = ExchangeFactory.INSTANCE.createExchange(BitstampExchange::class.java)
    val feed = XChangePollingLiveFeed(exchange)
    println(feed.availableAssets.summary())

    feed.subscribeTrade("BTC_USD", pollingDelayMillis = 30_000)
    println("Subscribed")
    assertEquals("BTC_USD", feed.assets.first().symbol)

    /// Run it for 2 minutes
    val timeframe = Timeframe.next(2.minutes)
    val result = feed.filter<PriceAction>(timeframe = timeframe)
    feed.close()
    assertEquals(AssetType.CRYPTO, result.first().second.asset.type)
}

suspend fun main() {

    when ("WEB") {
        "RECORD" -> recordBinanceFeed()
        "USE" -> useBinanceFeed()
        "XCHANGE" -> xchangeFeed()
        "LIVE" -> binanceLiveFeed()
        "HISTORIC" -> binanceBackTest()
        "FORWARD" -> binanceForwardTest()
        "WEB" -> binanceWebServer()
    }
}