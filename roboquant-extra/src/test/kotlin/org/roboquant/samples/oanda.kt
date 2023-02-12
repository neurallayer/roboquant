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
import org.roboquant.brokers.*
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.loggers.InfoLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.oanda.*
import org.roboquant.orders.FOK
import org.roboquant.orders.MarketOrder
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import java.text.DecimalFormat

fun oandaHistoricFeed() {
    val feed = OANDAHistoricFeed()
    feed.retrieve("EUR_USD", "USD_JPY", "GBP_USD")
    println(feed.assets.summary())
    println("events=${feed.timeline.size} timeframe=${feed.timeframe}")
}


fun oandaRecord() {
    val feed = OANDAHistoricFeed()
    val timeframe = Timeframe.parse("2020-03-01", "2020-04-01")
    val symbols = listOf(
        "EUR_USD",
        "USD_JPY",
        "GBP_USD",
        "AUD_USD",
        "USD_CAD",
        "USD_CHF",
        "EUR_GBP",
        "AUD_JPY",
        "NZD_USD",
        "GBP_JPY"
    ).toTypedArray()

    // There is a limit on what we can download per API call, so we split it in individual days
    for (tf in timeframe.split(1.days)) {
        feed.retrieve(*symbols, timeframe = tf)
        println(feed.timeline.size)
        Thread.sleep(1000) // let's play nice and not overload things
    }
    println(feed.assets.summary())
    println(feed.timeline.size)
    println(feed.timeframe)

    // Now we store it in a local Avro file for later reuse
    AvroFeed.record(feed, "/tmp/forex_march_2020.avro")

}


fun oandaRecord2() {
    val feed = OANDAHistoricFeed()
    val timeframe = Timeframe.parse("2022-01-01", "2023-01-01")
    val symbols = arrayOf("EUR_USD")

    // There is a limit on what we can download per API call, so we split it in individual days
    for (tf in timeframe.split(1.days)) {
        feed.retrieve(*symbols, timeframe = tf)
        println(feed.timeline.size)
        Thread.sleep(100) // let's play nice and not overload things
    }
    println(feed.assets.summary())
    println(feed.timeline.size)
    println(feed.timeframe)

    // Now we store it in a local Avro file for later reuse
    AvroFeed.record(feed, "/tmp/forex_pricebar_v5.1.avro")
}



fun forexAvro() {
    val feed = AvroFeed("/tmp/forex_march_2020.avro")
    Config.exchangeRates = OANDAExchangeRates()
    val strategy = EMAStrategy()
    val policy = FlexPolicy(shorting = true)
    val broker = OANDA.createSimBroker()
    val roboquant = Roboquant(strategy, AccountMetric(), policy = policy, broker = broker)
    roboquant.run(feed)
    println(roboquant.broker.account.summary())
}


fun oandaExchangeRates() {
    Currency.increaseDigits(3)
    val feed = OANDAHistoricFeed()
    feed.retrieve("EUR_USD", "GBP_USD")
    Config.exchangeRates = FeedExchangeRates(feed)

    val policy = FlexPolicy(shorting = true)
    val broker = OANDA.createSimBroker()
    val roboquant = Roboquant(EMAStrategy(), AccountMetric(), policy = policy, broker = broker)
    roboquant.run(feed)
    println(roboquant.broker.account.fullSummary())
}

fun oandaLiveOrderBook() {
    val feed = OANDALiveFeed()
    println(feed.availableAssets.size)
    val symbols = feed.availableAssets.take(25).symbols
    feed.subscribeOrderBook(*symbols, delay = 1_000)
    val tf = Timeframe.next(60.minutes)
    val formatter = DecimalFormat("######.##")
    val actions = feed.filter<OrderBook>(tf) {
        val pip = formatter.format(it.spread * 10_000)
        val price = formatter.format(it.getPrice())
        println("symbol=${it.asset.symbol} price=$price spread=$pip")
        true
    }
    println(actions.size)
}


fun oandaLiveRecord() {
    val feed = OANDALiveFeed()

    // Get all the FOREX pairs
    val symbols = feed.availableAssets.filter { it.type == AssetType.FOREX }.map { it.symbol }.toTypedArray()
    feed.subscribeOrderBook(*symbols, delay = 1_000)

    // Record for the next 60 minutes
    val tf = Timeframe.next(60.minutes)
    AvroFeed.record(feed, "/tmp/oanda_forex.avro", timeframe = tf)
}

fun oandaLivePriceBar() {
    val feed = OANDALiveFeed()
    feed.subscribePriceBar("EUR_USD", "USD_JPY", "GBP_USD", granularity = "S5", delay = 5_000L)
    val tf = Timeframe.next(15.minutes)
    val actions = feed.filter<PriceBar>(tf) {
        println(it)
        true
    }
    println(actions.size)
}


fun oandaBroker(closePositions: Boolean = false) {
    Config.exchangeRates = OANDAExchangeRates()
    val broker = OANDABroker()

    println(broker.account.fullSummary())
    println(broker.availableAssets.summary("Available Assets"))

    val roboquant = Roboquant(
        strategy = EMAStrategy(),
        AccountMetric(), ProgressMetric(),
        policy = FlexPolicy(shorting = true),
        broker = broker,
        logger = InfoLogger(splitMetrics = false)
    )

    val feed = OANDALiveFeed()
    val currencies = listOf(Currency.EUR, Currency.USD, Currency.GBP, Currency.JPY)
    val symbols = broker.availableAssets.filter { it.currency in currencies && it.type != AssetType.CRYPTO }.symbols
    feed.subscribeOrderBook(*symbols)
    val tf = Timeframe.next(60.minutes)
    roboquant.run(feed, tf)

    // Close all open positions
    if (closePositions) {
        val sizes = broker.account.positions.closeSizes
        val orders = sizes.map { MarketOrder(it.key, it.value) }
        broker.place(orders, Event.empty())
    }
    println(broker.account.fullSummary())
    feed.close()

}

fun oandaBroker3() {
    Currency.increaseDigits(3)

    val broker = OANDABroker()
    val account = broker.account
    println(account.fullSummary())
    val feed = OANDALiveFeed()
    feed.subscribeOrderBook("GBP_USD", "EUR_USD", "EUR_GBP")
    feed.heartbeatInterval = 30_000L

    val strategy = EMAStrategy() // Use EMA Crossover strategy
    val policy = FlexPolicy(shorting = true) // We want to short if we do Forex trading
    val roboquant = Roboquant(strategy, AccountMetric(), policy = policy, broker = broker)
    val timeframe = Timeframe.next(1.minutes) // restrict the time from now for the next minutes
    roboquant.run(feed, timeframe)
    println(account.fullSummary())
}

fun oandaBroker2(createOrder: Boolean = false) {
    Config.exchangeRates = OANDAExchangeRates()
    val broker = OANDABroker()
    println(broker.account.fullSummary())
    println(broker.availableAssets.summary())

    if (createOrder) {
        val asset = broker.availableAssets.findBySymbols("EUR_USD").first()
        val order = MarketOrder(asset, Size(-100), tif = FOK())
        broker.place(listOf(order), Event.empty())
        println(broker.account.fullSummary())
    }
}

fun main() {
    when ("OANDA_RECORD2") {
        "OANDA_BROKER" -> oandaBroker()
        "OANDA_BROKER2" -> oandaBroker2()
        "OANDA_BROKER3" -> oandaBroker3()
        "OANDA_FEED" -> oandaHistoricFeed()
        "OANDA_EXCHANGE_RATES" -> oandaExchangeRates()
        "OANDA_RECORD" -> oandaRecord()
        "OANDA_RECORD2" -> oandaRecord2()
        "OANDA_AVRO" -> forexAvro()
        "OANDA_LIVE_PRICEBAR" -> oandaLivePriceBar()
        "OANDA_LIVE_ORDERBOOK" -> oandaLiveOrderBook()
        "OANDA_LIVE_RECORD" -> oandaLiveRecord()
    }
}

