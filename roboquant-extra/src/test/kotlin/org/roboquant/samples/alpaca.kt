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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.alpaca.*
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.avro.AvroUtil
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import java.util.logging.Level

fun alpacaBroker() {
    val feed = CSVFeed("data/US") {
        priceAdjust = true
    }
    val broker = AlpacaBroker()
    val strategy = EMACrossover.EMA_12_26
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    roboquant.run(feed)
    broker.account.summary().print()
}

fun allAlpaca() {
    val broker = AlpacaBroker()
    val account = broker.account
    account.summary().log()
    account.summary().log()
    account.positions.summary().log()

    val feed = AlpacaLiveFeed()
    feed.heartbeatInterval = 30_000

    // Let trade all the assets that start with an A or are in our portfolio already
    // val assets = feed.availableAssets.filter { it.symbol.startsWith("AA") } + account.portfolio.assets
    val assets = account.assets
    feed.subscribeStocks(assets.distinct())

    val strategy = EMACrossover(3, 5)
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    val tf = Timeframe.next(15.minutes)
    roboquant.run(feed, tf)
    feed.close()

    roboquant.broker.account.summary().log()
}

fun alpacaConnection() {
    val config = AlpacaConfig()
    val api = AlpacaConnection.getAPI(config)
    println(AlpacaConnection.getAvailableAssets(api).size)
    println(AlpacaConnection.getAvailableAssets(api).values.filter { it.type == AssetType.CRYPTO })

}

fun alpacaLiveFeed() {
    Logging.setLevel(Level.FINER)
    val feed = AlpacaLiveFeed()

    // feed.subscribeStocks(listOf("TSLA", "IBM", "AMZN", "MSFT"))
    feed.subscribeCrypto(listOf("*"))
    feed.heartbeatInterval = 30_000
    val strategy = EMACrossover.EMA_5_15
    val roboquant = Roboquant(strategy, ProgressMetric())
    val tf = Timeframe.next(10.minutes)
    roboquant.run(feed, tf)
    feed.close()
    roboquant.broker.account.summary().log()
}

/**
 * Alpaca historic feed example where we retrieve 1 minutes price-bars in batches of 1 day for 100 days.
 */
fun alpacaHistoricFeed() {
    val feed = AlpacaHistoricFeed()
    val tf = Timeframe.past(100.days) - 15.minutes
    tf.split(1.days).forEach {
        feed.retrieve("AAPL", "IBM", timeframe = it, barSize = 1.minutes)
        println("timeline size is ${feed.timeline.size}")

        // Sleep a little to not exceed API call limits
        Thread.sleep(1000)
    }
    println("total timeframe is ${feed.timeframe}")

    // store the result in an Avro feed for future usage
    AvroUtil.record(feed, "/tmp/alpaca.avro")
}

fun alpacaHistoricFeed2() {
    val symbols = listOf(
        "AAPL",
        "FB",
        "IBM",
        "JPM",
        "MSFT",
        "TSLA",
        "GOOGL",
        "AMZN",
        "BRK.A",
        "V",
        "BABA",
        "NVDA",
        "JNJ",
        "TSM",
        "WMT"
    ).toTypedArray()
    val feed = AlpacaHistoricFeed()

    // We get the data for last 200 days. The minus 15.minutes is to make sure we only request data that
    // the free subscriptions is entitled to and not the latest 15 minutes.
    val tf = Timeframe.past(200.days) - 15.minutes

    feed.retrieve(*symbols, timeframe = tf)

    // Example on how to retreive 5 minutes bars
    // val tf = Timeframe.past(5.days) - 15.minutes
    // feed.retrieve("AAPL", "FB", "IBM", timeframe = tf, barSize = 5.minutes)

    feed.assets.summary().print()
}

fun main() {
    when ("ALPACA_HISTORIC_FEED") {
        "ALPACA_BROKER" -> alpacaBroker()
        "ALPACA_LIVE_FEED" -> alpacaLiveFeed()
        "ALPACA_CONNECTION" -> alpacaConnection()
        "ALPACA_HISTORIC_FEED" -> alpacaHistoricFeed()
        "ALPACA_HISTORIC_FEED2" -> alpacaHistoricFeed2()
        "ALPACA_ALL" -> allAlpaca()
    }
}