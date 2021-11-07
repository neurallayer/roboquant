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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.alpaca.*
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.common.*
import org.roboquant.feeds.OrderBook
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.filter
import org.roboquant.iex.Range
import org.roboquant.yahoo.YahooHistoricFeed
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.OpenPositions
import org.roboquant.metrics.ProgressMetric
import org.roboquant.oanda.OANDABroker
import org.roboquant.oanda.OANDAHistoricFeed
import org.roboquant.oanda.OANDALiveFeed
import org.roboquant.strategies.EMACrossover
import java.time.temporal.ChronoUnit


fun alpacaBroker() {
    val feed = CSVFeed("data/US", CSVConfig(priceAdjust = true))
    val broker = AlpacaBroker()
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    roboquant.run(feed)
    broker.account.summary().print()
}


fun allAlpaca() {
    val feed = AlpacaLiveFeed()
    feed.subscribe()

    feed.timeMillis = 1000
    val strategy = EMACrossover.shortTerm()
    val broker = AlpacaBroker()
    broker.account.summary().print()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), broker = broker)
    val tf = TimeFrame.nextMinutes(10)
    roboquant.run(feed, tf)
    roboquant.broker.account.summary().print()
    roboquant.logger.summary(3).print()
    feed.disconnect()
}


fun alpacaHistoricFeed() {
    val feed = AlpacaHistoricFeed()
    val tf = TimeFrame.pastPeriod(100, ChronoUnit.DAYS).minus(15)
    feed.retrieve("AAPL", "IBM", timeFrame = tf, period = AlpacaPeriod.DAY)
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
    roboquant.logger.summary().log()
}

fun alpacaFeed() {
    // Logging.setLevel(Level.FINER)
    val feed = AlpacaLiveFeed()
    val assets = feed.assets.take(50)
    feed.subscribe(assets)
    feed.timeMillis = 1000
    val strategy = EMACrossover.midTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric())
    val tf = TimeFrame.nextMinutes(5)
    roboquant.run(feed, tf)
    feed.disconnect()

    roboquant.broker.account.summary().log()
    roboquant.logger.summary().log()

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
    roboquant.run(feed, TimeFrame.nextMinutes(2))
    roboquant.broker. account.summary().log()
    roboquant.logger.summary().log()
}


fun feedYahoo() {

    val feed = YahooHistoricFeed()
    val apple = Asset("AAPL")
    val google = Asset("GOOG")
    val last300Days = TimeFrame.pastPeriod(300)
    feed.retrieve(apple, google, timeFrame = last300Days)

    val strategy = EMACrossover()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = logger)
    roboquant.run(feed)
    logger.summary(10)
}

fun oanda() {
    val feed = OANDAHistoricFeed()
    feed.retrieveCandles("EUR_USD", "USD_JPY", "GBP_USD")
    feed.assets.summary().log()
    println(feed.timeline.size)
    println(feed.timeFrame)

}



fun oandaLive() {
    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("EUR_USD", "USD_JPY", "GBP_USD")
    val tf = TimeFrame.nextMinutes(2)
    val actions = feed.filter<OrderBook>(tf)
    println(actions.size)

}


fun oandaLivePrices() {
    val feed = OANDALiveFeed()

    feed.subscribePrices("EUR_USD", "USD_JPY", "GBP_USD")
    println("starting")
    val data = feed.filter<OrderBook>(TimeFrame.nextMinutes(1))
    data.summary().log()
}


fun oandaBroker() {
    val currencyConverter = FixedExchangeRates(Currency.EUR, Currency.USD to 0.9, Currency.GBP to 1.2)
    val broker = OANDABroker(currencyConverter = currencyConverter)
    broker.account.summary().log()
    broker.account.portfolio.summary().log()
    broker.availableAssets.values.summary().log()

    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)

    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("EUR_USD", "GBP_USD")
    val twoMinutes = TimeFrame.nextMinutes(2)
    roboquant.run(feed, twoMinutes)
    broker.account.portfolio.summary().log()
}


fun main() {
    when ("OANDA_FEED") {
        "IEX" -> feedIEX()
        "IEX_LIVE" -> feedIEXLive()
        "YAHOO" -> feedYahoo()
        "ALPACA_BROKER" -> alpacaBroker()
        "ALPACA_FEED" -> alpacaFeed()
        "ALPACA_HISTORIC_FEED" -> alpacaHistoricFeed()
        "ALPACA_ALL" -> allAlpaca()
        "OANDA_BROKER" -> oandaBroker()
        "OANDA_FEED" -> oanda()
        "OANDA_LIVE_FEED" -> oandaLive()
        "OANDA_LIVE_PRICES" -> oandaLivePrices()
    }
}