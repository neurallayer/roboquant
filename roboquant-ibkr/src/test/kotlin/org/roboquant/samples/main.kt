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
import org.roboquant.brokers.Broker
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.assets
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.ibkr.IBKR
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.*
import org.roboquant.strategies.EMAStrategy
import java.time.Instant


private fun broker() {
    Config.exchangeRates = FixedExchangeRates(Currency.USD, Currency.EUR to 1.1)
    val broker = IBKRBroker()

    println(broker.account.fullSummary())
    Thread.sleep(5000)
    println(broker.account.positions.assets)
    broker.disconnect()
}

private fun closePosition() {
    val broker = IBKRBroker()
    val account = broker.account
    println(account.fullSummary())

    // Place a new market sell order
    val position = account.positions.first()
    val order = MarketOrder(position.asset, -position.size)
    broker.place(listOf(order), account.lastUpdate)
    Thread.sleep(10_000)
    println(account.fullSummary())
    broker.disconnect()
}

private fun showAccount() {

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


private fun placeOrder() {
    val broker = IBKRBroker()
    Config.exchangeRates = broker.exchangeRates
    val account = broker.account
    println(account.fullSummary())

    val asset = Asset("TSLA", AssetType.STOCK, "USD", "SMART")
    val order = BracketOrder.limitTrailStop(
        asset,
        Size.ONE,
        185.50
    )

    broker.place(listOf(order), Instant.now())
    Thread.sleep(5_000)
    val account2 = broker.account
    println(account2.fullSummary())
    broker.disconnect()
    println("done")
}


private fun Broker.place(order: Order) {
    place(listOf(order), Instant.now())
    Thread.sleep(5_000)
    sync()
    println(account.fullSummary())
}

private fun placeSimpleOrders() {
    val asset = Asset("TSLA", AssetType.STOCK, "USD")

    // Link the asset to an IBKR contract-id.
    IBKR.register(76792991, asset)

    val broker = IBKRBroker()
    Config.exchangeRates = broker.exchangeRates
    val account = broker.account
    println(account.fullSummary())


    // Place a buy order
    val buy = MarketOrder(asset, Size.ONE)
    broker.place(buy)

    // Place a sell order
    val sell = TrailOrder(asset, -Size.ONE, 1.percent)
    broker.place(sell)

    // Cancel the sell order
    val order = broker.account.openOrders.last()
    val cancel = CancelOrder(order)
    broker.place(cancel)

    // Place another sell order
    val sell2 = MarketOrder(asset, -Size.ONE)
    broker.place(sell2)

    broker.disconnect()
    println("done")
}


private fun simplePaperTrade() {
    // Lets trade these 3 tech stock
    val tsla = Asset("TSLA", AssetType.STOCK, "USD")
    val msft = Asset("MSFT", AssetType.STOCK, "USD")
    val googl = Asset("GOOGL", AssetType.STOCK, "USD")

    // Link the asset to an IBKR contract-id.
    IBKR.register(76792991, tsla)
    IBKR.register(272093, msft)
    IBKR.register(208813719, googl)

    val broker = IBKRBroker()
    Config.exchangeRates = broker.exchangeRates
    val account = broker.account
    println(account.fullSummary())

    val feed = IBKRLiveFeed { client = 3 }
    feed.subscribe(tsla, msft, googl)

    val strategy = EMAStrategy.PERIODS_12_26
    val rq = Roboquant(strategy, AccountMetric(), ProgressMetric(), broker = broker, logger = ConsoleLogger())
    rq.run(feed, Timeframe.next(2.hours))

    feed.disconnect()
    broker.disconnect()
    println("done")
}

private fun liveFeedEU() {
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

private fun liveFeedUS() {
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

private fun historicFeed() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { Asset(it, AssetType.STOCK, "EUR", "AEB") }
    feed.retrieve(assets)
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}


private fun historicFeed2() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscription for European stocks
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { Asset(it, AssetType.STOCK, "EUR", "AEB") }
    feed.retrieve(assets, duration = "5 D", barSize = "1 min")
    feed.waitTillRetrieved()
    println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
    feed.disconnect()
}


private fun historicFuturesFeed() {
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


internal fun main() {

    when ("SIMPLE_PAPER_TRADE") {
        "ACCOUNT" -> showAccount()
        "BROKER" -> broker()
        "CLOSE_POSITION" -> closePosition()
        "LIVE_FEED_EU" -> liveFeedEU()
        "LIVE_FEED_US" -> liveFeedUS()
        "HISTORIC" -> historicFeed()
        "HISTORIC2" -> historicFeed2()
        "HISTORIC3" -> historicFuturesFeed()
        "PLACE_ORDER" -> placeOrder()
        "PLACE_SIMPLE_ORDER" -> placeSimpleOrders()
        "SIMPLE_PAPER_TRADE" -> simplePaperTrade()
    }

}
