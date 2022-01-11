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
import org.roboquant.brokers.FeedExchangeRates
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.Portfolio
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.OrderBook
import org.roboquant.feeds.avro.AvroUtil
import org.roboquant.feeds.filter
import org.roboquant.metrics.AccountSummary
import org.roboquant.oanda.*
import org.roboquant.orders.FOK
import org.roboquant.orders.MarketOrder
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMACrossover
import java.util.logging.Level


fun oanda() {
    val feed = OANDAHistoricFeed()
    feed.retrieveCandles("EUR_USD", "USD_JPY", "GBP_USD")
    feed.assets.summary().log()
    println(feed.timeline.size)
    println(feed.timeFrame)

}

fun oandaLong() {
    val feed = OANDAHistoricFeed()
    val timeFrame = TimeFrame.parse("2020-01-01", "2020-02-01")

    // There is a limit on what we can download per API call, so we split it in individual days
    for (tf in timeFrame.split(1.days)) {
        feed.retrieveCandles("EUR_USD", "USD_JPY", "GBP_USD", timeFrame = tf)
    }
    feed.assets.summary().log()
    println(feed.timeline.size)
    println(feed.timeFrame)

    // Now we store it in a local Avro file
    AvroUtil.record(feed, "/Users/peter/data/avro/forex.avro")

}


fun oanda2() {
    Currency.increaseDigits(3)
    val feed = OANDAHistoricFeed()
    feed.retrieveCandles("EUR_USD", "EUR_GBP", "GBP_USD")
    Config.exchangeRates = FeedExchangeRates(feed)

    val roboquant = OANDA.roboquant(EMACrossover())
    roboquant.run(feed)
    roboquant.broker.account.fullSummary().print()
}


fun oandaLive() {
    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("EUR_USD", "USD_JPY", "GBP_USD")
    val tf = TimeFrame.next(5.minutes)
    val actions = feed.filter<OrderBook>(tf)
    println(actions.size)
}



fun oandaPaperTrading() {
    Currency.increaseDigits(3) // We want to use extra digits when displaying amounts
    val broker = OANDABroker()
    Config.exchangeRates = OANDAExchangeRates.allAvailableAssets()

    val feed = OANDALiveFeed()
    val assets = broker.availableAssets.findByCurrencies("EUR", "USD", "JPY", "GBP", "CAD", "CHF")
    feed.subscribeOrderBook(assets)

    val policy = DefaultPolicy(shorting = true)
    val roboquant = Roboquant(EMACrossover.shortTerm(), broker = broker, policy = policy)

    val tf = TimeFrame.next(5.minutes)
    roboquant.run(feed, tf)

    roboquant.broker.account.fullSummary().print()
}


fun oandaClosePositions() {
    val broker = OANDABroker()
    Logging.setLevel(Level.FINE)
    Config.exchangeRates = OANDAExchangeRates.allAvailableAssets()

    val target = Portfolio() // target portfolio is an empty portfolio
    val changes = broker.account.portfolio.diff(target)
    val orders = changes.map { MarketOrder(it.key, it.value) }
    broker.place(orders, Event.empty())
    broker.account.fullSummary().print()
}



fun oandaLiveRecord() {
    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("EUR_USD", "USD_JPY", "GBP_USD")
    val tf = TimeFrame.next(5.minutes)
    AvroUtil.record(feed, "/Users/peter/tmp/oanda.avro", tf)
}


fun oandaLivePrices() {
    Logging.setLevel(Level.FINE, "org.roboquant")
    val feed = OANDALiveFeed()
    feed.subscribePrices("EUR_USD", "USD_JPY", "GBP_USD")
    val data = feed.filter<OrderBook>(TimeFrame.next(1.minutes))
    data.summary().log()
}


fun oandaBroker() {
    Config.exchangeRates = FixedExchangeRates(Currency.EUR, Currency.USD to 0.9, Currency.GBP to 1.2)
    val broker = OANDABroker()
    broker.account.summary().log()
    broker.account.portfolio.summary().log()
    broker.availableAssets.summary().log()

    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)

    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("EUR_USD", "GBP_USD", "GBP_EUR")
    val twoMinutes = TimeFrame.next(5.minutes)
    roboquant.run(feed, twoMinutes)
    broker.account.portfolio.summary().log()
}


fun oandaBroker3() {
    Logging.setLevel(Level.FINE, "OANDABroker")
    Currency.increaseDigits(3)

    val broker = OANDABroker(enableOrders = true)
    val account = broker.account
    account.fullSummary().print()
    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("GBP_USD", "EUR_USD", "EUR_GBP")
    feed.heartbeatInterval = 30_000L

    val strategy = EMACrossover() // Use EMA Crossover strategy
    val policy = DefaultPolicy(shorting = true) // We want to short if we do Forex trading
    val roboquant = Roboquant(strategy, AccountSummary(), policy = policy, broker = broker)
    val timeFrame = TimeFrame.next(1.minutes) // restrict the time from now for the next minutes
    roboquant.run(feed, timeFrame)
    account.fullSummary().print()
}

fun oandaBroker2(createOrder: Boolean = true) {
    Logging.setLevel(Level.FINE, "OANDABroker")
    val broker = OANDABroker(enableOrders = true)
    broker.account.fullSummary().log()
    broker.availableAssets.summary().log()

    if (createOrder) {
        val asset = broker.availableAssets.findBySymbols("EUR_USD").first()
        val order = MarketOrder(asset, -100.0, tif = FOK())
        broker.place(listOf(order), Event.empty())
        broker.account.fullSummary().log()
    }
}


fun main() {
    when ("OANDA_PAPER") {
        "OANDA_BROKER" -> oandaBroker()
        "OANDA_BROKER2" -> oandaBroker2()
        "OANDA_BROKER3" -> oandaBroker3()
        "OANDA_FEED" -> oanda()
        "OANDA_FEED2" -> oanda2()
        "OANDA_FEED3" -> oandaLong()
        "OANDA_LIVE_FEED" -> oandaLive()
        "OANDA_LIVE_RECORD" -> oandaLiveRecord()
        "OANDA_LIVE_PRICES" -> oandaLivePrices()
        "OANDA_PAPER" -> oandaPaperTrading()
        "OANDA_CLOSE" -> oandaClosePositions()
    }
}

