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

import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.assets
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRExchangeRates
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.orders.BracketOrder
import org.roboquant.orders.MarketOrder

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

    // Place a new market sell order
    val position = account.positions.first()
    val order = MarketOrder(position.asset, -position.size)
    broker.place(listOf(order))
    Thread.sleep(10_000)
    println(account.fullSummary())
    broker.disconnect()
}

fun showAccount() {

    // If you have multiple currencies in your trading-account, you need to set up exchange rates
    // Config.exchangeRates = IBKRExchangeRates()

    // Get the account object from the broker instance
    val broker = IBKRBroker()
    val account = broker.account

    // Print the full summary of the account
    println(account.fullSummary())

    // Disconnect
    broker.disconnect()
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

    broker.place(listOf(order))
    Thread.sleep(5_000)
    val account2 = broker.account
    println(account2.fullSummary())
    broker.disconnect()
    println("done")
}



fun placeSimpleOrder() {
    Config.exchangeRates = IBKRExchangeRates()
    val broker = IBKRBroker()
    broker.sync()
    val account = broker.account
    println(account.fullSummary())

    val asset = Asset("TSLA", AssetType.STOCK, "USD", "SMART")
    val order = MarketOrder(asset, Size.ONE)

    broker.place(listOf(order))
    Thread.sleep(5000)
    broker.sync()
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

    when ("PLACE_SIMPLE_ORDER") {
        "ACCOUNT" -> showAccount()
        "EXCH" -> exchangeRates()
        "BROKER" -> broker()
        "CLOSE_POSITION" -> closePosition()
        "LIVE_FEED_EU" -> liveFeedEU()
        "LIVE_FEED_US" -> liveFeedUS()
        "HISTORIC" -> historicFeed()
        "HISTORIC2" -> historicFeed2()
        "HISTORIC3" -> historicFuturesFeed()
        "PLACE_ORDER" -> placeOrder()
        "PLACE_SIMPLE_ORDER" -> placeSimpleOrder()
    }

}