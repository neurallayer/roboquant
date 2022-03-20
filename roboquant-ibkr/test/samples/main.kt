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
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.EventRecorder
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.EMACrossover
import java.util.logging.Level

fun ibkrBroker() {
    Config.exchangeRates = FixedExchangeRates(Currency.USD, Currency.EUR to 1.1)

    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(asset)
    val broker = IBKRBroker()
    broker.account.fullSummary().print()

    val strategy = EMACrossover.EMA_12_26
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    val tf = Timeframe.next(3.minutes)
    roboquant.run(feed, tf)
    broker.account.summary()

    // Disconnect
    broker.disconnect()
    feed.disconnect()
}


fun closePosition() {
    Logging.setLevel(Level.FINE)
    Logging.useSimpleFormat = false
    val broker = IBKRBroker(enableOrders = true)
    val account = broker.account
    account.fullSummary().print()

    // Now lets place a new market sell order
    val position = account.positions.first()
    val order = MarketOrder(position.asset, -position.size)
    broker.place(listOf(order), Event.empty())
    Thread.sleep(10_000)
    account.fullSummary().print()
    broker.disconnect()
}

fun ibkrBrokerFeed() {

    val exchangeRates = FixedExchangeRates(Currency.EUR, Currency.USD to 0.89)
    Config.exchangeRates = exchangeRates
    val broker = IBKRBroker()
    broker.account.positions.summary().log()

    // Subscribe to all assets in the portfolio
    val feed = IBKRLiveFeed()
    feed.subscribe(broker.account.assets)

    val strategy = EMACrossover.EMA_5_15

    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), broker = broker)
    val tf = Timeframe.next(1.minutes)
    roboquant.run(feed, tf)
    broker.account.summary().log()
    broker.disconnect()
    feed.disconnect()
}


fun ibkrFeed() {
    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(asset)

    val cash = Wallet(1_000_000.EUR)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.EMA_5_15
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), EventRecorder(), broker = broker)
    val tf = Timeframe.next(10.minutes)

    roboquant.run(feed, tf)
    feed.disconnect()
}


fun ibkrHistoricFeedEU() {
    val feed = IBKRHistoricFeed()

    // This assumes you have a valid market subscriptoin for European stocks
    val template = Asset("TEMPLATE", AssetType.STOCK, "EUR", "AEB")
    val symbols = listOf("ABN", "ASML", "KPN")
    val assets = symbols.map { template.copy(symbol = it) }
    feed.retrieve(assets)
    feed.waitTillRetrieved()

    val inititalDeposit = Wallet(100_000.EUR)
    val broker = SimBroker(inititalDeposit)
    val strategy = EMACrossover.EMA_5_15
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
    feed.disconnect()
}


fun main() {

    when ("BROKER") {
        "BROKER" -> ibkrBroker()
        "CLOSE_POSITION" -> closePosition()
        "FEED" -> ibkrFeed()
        "BROKER_FEED" -> ibkrBrokerFeed()
        "HISTORIC" -> ibkrHistoricFeedEU()
    }

}