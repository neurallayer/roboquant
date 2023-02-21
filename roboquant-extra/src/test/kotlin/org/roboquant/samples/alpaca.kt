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

import org.roboquant.Roboquant
import org.roboquant.alpaca.*
import org.roboquant.common.*
import org.roboquant.feeds.AvroFeed
import org.roboquant.loggers.InfoLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import java.time.Instant
import kotlin.io.path.div

fun alpacaBroker() {
    val broker = AlpacaBroker()
    println(broker.account.fullSummary())
}

fun alpacaPaperTradeStocks() {
    val broker = AlpacaBroker()
    val account = broker.account
    println(account.fullSummary())

    val feed = AlpacaLiveFeed()

    feed.heartbeatInterval = 30_000


    // Lets pick 10 random stock symbols to trade
    val symbols = feed.availableStocks.random(10).symbols
    feed.subscribeStocks(*symbols)

    val strategy = EMAStrategy(3, 5)
    val roboquant = Roboquant(strategy, AccountMetric(), ProgressMetric(), broker = broker, logger = InfoLogger())
    val tf = Timeframe.next(60.minutes)
    roboquant.run(feed, tf)
    feed.close()

    println(roboquant.broker.account.fullSummary())
}


fun alpacaTradeCrypto() {
    val feed = AlpacaLiveFeed()
    val symbols = feed.availableCrypto.random(10).symbols
    println(symbols.toList())

    feed.subscribeCrypto(*symbols)
    feed.heartbeatInterval = 30_000
    val strategy = EMAStrategy.PERIODS_5_15
    val roboquant = Roboquant(strategy, ProgressMetric())
    val tf = Timeframe.next(10.minutes)
    roboquant.run(feed, tf)
    feed.close()
    println(roboquant.broker.account.summary())
}


fun alpacaTradeStocks() {
    val feed = AlpacaLiveFeed()

    val symbols = feed.availableStocks.take(10).symbols
    println(symbols.toList())

    feed.subscribeStocks(*symbols, type = PriceActionType.QUOTE)
    feed.heartbeatInterval = 30_000
    val strategy = EMAStrategy.PERIODS_5_15
    val roboquant = Roboquant(strategy, AccountMetric(), ProgressMetric(), logger = InfoLogger())
    val tf = Timeframe.next(10.minutes)
    roboquant.run(feed, tf)
    feed.close()
    println(roboquant.broker.account.summary())
}

/**
 * Alpaca historic feed example where we retrieve 1 minutes price-bars in batches of 1 day for 100 days.
 */
fun alpacaHistoricFeed() {
    val feed = AlpacaHistoricFeed()
    val tf = Timeframe.past(100.days) - 15.minutes
    tf.split(1.days).forEach {
        feed.retrieveStockPriceBars("AAPL", "IBM", timeframe = it, barPeriod = BarPeriod.MINUTE)
        println("timeline size is ${feed.timeline.size}")

        // Sleep a little to not exceed API call limits
        Thread.sleep(1000)
    }
    println("total timeframe is ${feed.timeframe}")

    // store the result in an Avro feed for future usage
    AvroFeed.record(feed, "/tmp/alpaca.avro")
}


/**
 * Alpaca historic feed
 */
fun alpacaSP500PriceBar() {
    val feed = AlpacaHistoricFeed()
    val now = Instant.now()
    val tf = Timeframe(Instant.parse("2000-01-01T00:00:00Z"), now)

    val feedSymbols = feed.availableAssets.symbols
    val symbols = Universe.sp500.getAssets(now).symbols.filter { it in feedSymbols }
    println("${symbols.size} symbols found")


    symbols.forEach {
        feed.retrieveStockPriceBars(it, timeframe = tf, barPeriod = BarPeriod.DAY)
        println("symbol=$it")

        // Sleep a little to not exceed API call limits
        Thread.sleep(200)
    }

    println("total timeframe is ${feed.timeframe}")

    // save the results in an Avro feed format for future usage
    val fileName = Config.home / "sp500_pricebar_v5.0.avro"
    AvroFeed.record(feed, fileName.toString())
}


/**
 * Alpaca historic feed
 */
fun alpacaSP500PriceQuote() {
    val feed = AlpacaHistoricFeed()
    val now = Instant.now()
    val tf = Timeframe.parse("2022-11-14T18:00:00Z", "2022-11-14T18:05:00Z") // 5 minutes of data

    val feedSymbols = feed.availableAssets.symbols
    val symbols = Universe.sp500.getAssets(now).symbols.filter { it in feedSymbols }
    println("${symbols.size} symbols found")

    symbols.forEach {
        feed.retrieveStockQuotes(it, timeframe = tf)
        println("symbol=$it")

        // Sleep a little to not exceed API call limits
        Thread.sleep(200)
    }

    println("total timeframe is ${feed.timeframe}")

    // save the results in an Avro feed format for future usage
    val fileName = Config.home / "sp500_pricequote_v5.0.avro"
    AvroFeed.record(feed, fileName.toString())
}


fun alpacaHistoricFeed2() {
    val symbols = listOf(
        "AAPL",
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
    feed.retrieveStockPriceBars(*symbols, timeframe = tf)
    println(feed.assets.summary())
}

fun main() {
    when ("ALPACA_HISTORIC_FEED2") {
        "ALPACA_BROKER" -> alpacaBroker()
        "ALPACA_TRADE_CRYPTO" -> alpacaTradeCrypto()
        "ALPACA_TRADE_STOCKS" -> alpacaTradeStocks()
        "ALPACA_HISTORIC_FEED" -> alpacaHistoricFeed()
        "ALPACA_HISTORIC_FEED2" -> alpacaHistoricFeed2()
        "ALPACA_HISTORIC_SP500_PRICEBAR" -> alpacaSP500PriceBar()
        "ALPACA_HISTORIC_SP500_PRICEQUOTE" -> alpacaSP500PriceQuote()
        "ALPACA_PAPER_TRADE_STOCKS" -> alpacaPaperTradeStocks()
        "CREATE_SAMPLE_DATA" -> {
            alpacaSP500PriceBar(); alpacaSP500PriceQuote()
        }
    }
}