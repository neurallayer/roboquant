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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.alpaca.AlpacaBroker
import org.roboquant.alpaca.AlpacaHistoricFeed
import org.roboquant.alpaca.AlpacaLiveFeed
import org.roboquant.alpaca.AlpacaPeriod
import org.roboquant.common.*
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.iex.Range
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.roboquant.yahoo.YahooHistoricFeed


fun alpacaBroker() {
    val feed = CSVFeed("data/US", CSVConfig(priceAdjust = true))
    val broker = AlpacaBroker()
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    roboquant.run(feed)
    broker.account.summary().print()
}


fun allAlpaca() {
    val broker = AlpacaBroker()
    val account = broker.account
    account.summary().log()
    account.orders.summary().log()
    account.portfolio.summary().log()

    val feed = AlpacaLiveFeed()
    feed.heartbeatInterval = 1000


    // Let trade all the assets that start with an A or are in our portfolio already
    // val assets = feed.availableAssets.filter { it.symbol.startsWith("AA") } + account.portfolio.assets
    val assets = account.portfolio.assets
    feed.subscribe(assets.distinct())

    val strategy = EMACrossover(3, 5)
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    val tf = Timeframe.next(15.minutes)
    roboquant.run(feed, tf)
    feed.close()

    roboquant.broker.account.summary().log()
}


fun alpacaHistoricFeed() {
    val feed = AlpacaHistoricFeed()
    val tf = Timeframe.past(100.days) - 15.minutes
    feed.retrieveBars("AAPL", "IBM", timeframe = tf, period = AlpacaPeriod.DAY)
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
}

fun alpacaFeed() {
    // Logging.setLevel(Level.FINER)
    val feed = AlpacaLiveFeed()
    val assets = feed.assets.take(50)
    feed.subscribe(assets)
    feed.heartbeatInterval = 1000
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    val tf = Timeframe.next(5.minutes)
    roboquant.run(feed, tf)
    feed.close()

    roboquant.broker.account.summary().log()
}

fun feedIEX() {
    val token = Config.getProperty("IEX_PUBLIC_KEY") ?: throw Exception("No token found")
    val feed = org.roboquant.iex.IEXHistoricFeed(token)
    feed.retrievePriceBar("AAPL", "GOOGL", "FB", range = Range.TWO_YEARS)

    val strategy = EMACrossover(10, 30)
    val roboquant = Roboquant(strategy, AccountSummary())
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
}


fun feedIEXLive() {
    val feed = org.roboquant.iex.IEXLiveFeed()
    val apple = Asset("AAPL")
    val google = Asset("GOOG")
    feed.subscribeTrades(apple, google)

    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    roboquant.run(feed, Timeframe.next(5.minutes))
    roboquant.broker.account.summary().log()
}


fun feedYahoo() {
    val feed = YahooHistoricFeed()
    val apple = Asset("AAPL")
    val google = Asset("GOOG")
    val last300Days = Timeframe.past(300.days)
    feed.retrieve(apple, google, timeframe = last300Days)

    val strategy = EMACrossover()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = logger)
    roboquant.run(feed)
    logger.summary(10)
}



fun main() {
    when ("OANDA_PAPER") {
        "IEX" -> feedIEX()
        "IEX_LIVE" -> feedIEXLive()
        "YAHOO" -> feedYahoo()
        "ALPACA_BROKER" -> alpacaBroker()
        "ALPACA_FEED" -> alpacaFeed()
        "ALPACA_HISTORIC_FEED" -> alpacaHistoricFeed()
        "ALPACA_ALL" -> allAlpaca()
    }
}