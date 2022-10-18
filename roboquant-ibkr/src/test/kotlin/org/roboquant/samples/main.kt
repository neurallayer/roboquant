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
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRExchangeRates
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.EMAStrategy

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
    println(broker.account.assets)
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

fun paperTrade(minutes: Int = 10) {

    Config.exchangeRates = IBKRExchangeRates()
    val broker = IBKRBroker()
    println(broker.account.positions.summary())

    // Subscribe to all assets in the portfolio
    val feed = IBKRLiveFeed()
    feed.subscribe(broker.account.assets)

    val strategy = EMAStrategy.EMA_5_15

    val roboquant = Roboquant(strategy, AccountMetric(), ProgressMetric(), broker = broker)
    val tf = Timeframe.next(minutes.minutes)
    roboquant.run(feed, tf)
    println(broker.account.summary())
    broker.disconnect()
    feed.disconnect()
}

fun liveFeedEU() {
    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(asset)
    val tf = Timeframe.next(1.minutes)
    val data = feed.filter<PriceAction>(tf)
    println(data.size)
    feed.disconnect()
}

fun liveFeedUS() {
    val feed = IBKRLiveFeed()
    val asset = Asset("TSLA", AssetType.STOCK, "USD")
    feed.subscribe(asset)
    val tf = Timeframe.next(1.minutes)
    val data = feed.filter<PriceAction>(tf)
    println(data.size)
    feed.disconnect()
}

fun historicFeed() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val template = Asset("TEMPLATE", AssetType.STOCK, "EUR", "AEB")
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { template.copy(symbol = it) }
    feed.retrieve(assets)
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}

fun historicFeed2() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val template = Asset("TEMPLATE", AssetType.STOCK, "EUR", "AEB")
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { template.copy(symbol = it) }
    feed.retrieve(assets, duration = "2 D", barSize = "30 mins")
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
        "PAPER_TRADE" -> paperTrade(30)
        "HISTORIC" -> historicFeed()
        "HISTORIC2" -> historicFeed2()
    }

}