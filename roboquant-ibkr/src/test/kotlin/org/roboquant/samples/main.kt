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
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.assets
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRExchangeRates
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.BracketOrder
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.EMAStrategy
import java.time.DayOfWeek
import java.time.LocalDate

fun exchangeRates() {
    val exchangeRates = IBKRExchangeRates()
    println(exchangeRates.exchangeRates)
    exchangeRates.refresh()
    println(exchangeRates.exchangeRates)
}

fun broker() {
    Config.exchangeRates = FixedExchangeRates(Currency.USD, Currency.EUR to 1.1)
    val broker = IBKRBroker()
    println(broker.account.fullSummary())
    Thread.sleep(5000)
    println(broker.account.positions.assets)
    broker.disconnect()
}

fun closePosition() {
    val broker = IBKRBroker()
    val account = broker.account
    println(account.fullSummary())

    // Now lets place a new market sell order
    val position = account.positions.first()
    val order = MarketOrder(position.asset, -position.size)
    broker.place(listOf(order), Event.empty())
    Thread.sleep(10_000)
    println(account.fullSummary())
    broker.disconnect()
}

fun showAccount() {

    // If you have multiple currencies in your account
    // you need to set up exchange rates
    // Config.exchangeRates = IBKRExchangeRates()

    // Get the account object from the broker instance
    val broker = IBKRBroker()
    val account = broker.account


    // Print the full summary of the account
    println(account.fullSummary())

    // Disconnect
    broker.disconnect()
}

fun paperTrade() {
    Config.exchangeRates = IBKRExchangeRates()
    val broker = IBKRBroker()
    Thread.sleep(5_000)
    val account = broker.account
    println(account.fullSummary())

    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(listOf(asset))

    val strategy = EMAStrategy.PERIODS_5_15
    val roboquant = Roboquant(strategy, AccountMetric(), ProgressMetric(), broker = broker, logger = ConsoleLogger())
    val tf = Timeframe.next(60.minutes)
    roboquant.run(feed, tf)

    println(broker.account.fullSummary())
    feed.disconnect()
    broker.disconnect()
    println("done")
}


fun placeOrder() {
    Config.exchangeRates = IBKRExchangeRates()
    val broker = IBKRBroker()
    val account = broker.account
    println(account.fullSummary())

    val asset = Asset("TSLA", AssetType.STOCK, "USD", "SMART")
    val order = BracketOrder.limitTrailStop(
        asset,
        Size.ONE,
        185.50
    )

    broker.place(listOf(order), Event.empty())
    Thread.sleep(5_000)
    val account2 = broker.account
    println(account2.fullSummary())
    broker.disconnect()
    println("done")
}


fun liveFeedEU() {
    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(listOf(asset))
    val tf = Timeframe.next(10.minutes)
    val data = feed.filter<PriceAction>(tf) {
        println(it)
        true
    }
    println(data.size)
    feed.disconnect()
}

fun liveFeedUS() {
    val feed = IBKRLiveFeed()
    val asset = Asset("TSLA", AssetType.STOCK, "USD")
    feed.subscribe(listOf(asset))
    val tf = Timeframe.next(1.minutes)
    val data = feed.filter<PriceAction>(tf) {
        println(it)
        true
    }
    println(data.size)
    feed.disconnect()
}

fun historicFeed() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { Asset(it, AssetType.STOCK, "EUR", "AEB") }
    feed.retrieve(assets)
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}


fun retrieveBatch() {
    val feed = IBKRHistoricFeed()
    val symbols = listOf("ABN", "ASML", "KPN")
    val exchange = Exchange.AEB
    val assets = symbols.map { Asset(it, AssetType.STOCK, "EUR", exchange.exchangeCode) }
    var start = LocalDate.parse("2020-01-02")
    val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    repeat(200) {
        if (start.dayOfWeek !in weekend) {
            val closingTime = exchange.getClosingTime(start) + 6.hours
            feed.retrieve(assets, closingTime, "1 D", "1 min")
            feed.waitTillRetrieved()
            Thread.sleep(2000)

            println("events=${feed.toList().size} timeline=${feed.timeline.distinct().size} tf=${feed.timeframe.end}")
        }
        start = start.plusDays(1)
    }

    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()

    AvroFeed.record(feed, "/tmp/1_minute_aeb.avro")
}


fun historicFeed2() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { Asset(it, AssetType.STOCK, "EUR", "AEB") }
    feed.retrieve(assets, duration = "5 D", barSize = "1 min")
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}


fun historicFuturesFeed() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market data subscriptions for these futures
    val assets = listOf(
        Asset("FGBL MAR 23", AssetType.FUTURES, "EUR", "EUREX"),
        Asset("GCZ2", AssetType.FUTURES, "USD", "NYMEX"),
    )
    feed.retrieve(assets)
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}


fun main() {

    when ("ACCOUNT") {
        "ACCOUNT" -> showAccount()
        "EXCH" -> exchangeRates()
        "BROKER" -> broker()
        "CLOSE_POSITION" -> closePosition()
        "LIVE_FEED_EU" -> liveFeedEU()
        "LIVE_FEED_US" -> liveFeedUS()
        "PAPER_TRADE" -> paperTrade()
        "HISTORIC" -> historicFeed()
        "HISTORIC2" -> historicFeed2()
        "HISTORIC3" -> historicFuturesFeed()
        "BATCH" -> retrieveBatch()
        "PLACE_ORDER" -> placeOrder()
    }

}