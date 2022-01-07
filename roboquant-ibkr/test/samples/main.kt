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
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.PriceRecorder
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.EMACrossover
import java.util.logging.Level

fun ibkrBroker() {
    val feed = CSVFeed("data/US", CSVConfig(priceAdjust = true))
    val exchangeRates = FixedExchangeRates(Currency.USD, Currency.EUR to 1.2)
    val broker = IBKRBroker(exchangeRates = exchangeRates)

    val strategy = EMACrossover.midTerm()

    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    val tf = TimeFrame.next(3.minutes)
    roboquant.run(feed, tf)
    broker.account.summary()
    broker.disconnect()
}


fun ibkrBroker2() {
    Logging.setLevel(Level.FINE)
    Logging.useSimpleFormat = false
    val broker = IBKRBroker(enableOrders = true)
    val account = broker.account
    account.fullSummary().print()

    // Now lets place a new market sell order
    val asset = account.portfolio.assets.findBySymbols("AAPL").first()
    val order = MarketOrder(asset, -1.0)
    broker.place(listOf(order), Event.empty())
    Thread.sleep(10000)
    account.fullSummary().print()
    broker.disconnect()
}

fun ibkrBrokerFeed() {

    val exchangeRates = FixedExchangeRates(Currency.EUR, Currency.USD to 0.89)
    val broker = IBKRBroker(exchangeRates = exchangeRates)
    broker.account.portfolio.summary().log()

    // Subscribe to all assets in the portfolio
    val feed = IBKRLiveFeed()
    val assets = broker.account.portfolio.assets
    feed.subscribe(*assets.toTypedArray())

    val strategy = EMACrossover.shortTerm()

    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), broker = broker)
    val tf = TimeFrame.next(1.minutes)
    roboquant.run(feed, tf)
    broker.account.summary().log()
    roboquant.logger.summary().log()
    broker.disconnect()
    feed.disconnect()
}


fun ibkrFeed() {
    val feed = IBKRLiveFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(asset)

    val cash = Wallet(1_000_000.EUR)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.shortTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), PriceRecorder(asset), broker = broker)
    val tf = TimeFrame.next(10.minutes)

    roboquant.run(feed, tf)
    feed.disconnect()
    roboquant.logger.summary().log()
}


fun ibkrHistoricFeed() {
    val feed = IBKRHistoricFeed()
    val template = Asset("", AssetType.STOCK, "EUR", "AEB")
    feed.retrieve(template.copy(symbol = "ABN"), template.copy(symbol = "ASML"), template.copy(symbol = "KPN"))

    val cash = Wallet(1_000_000.EUR)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.shortTerm()
    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    roboquant.run(feed)
    roboquant.logger.summary().print()
    roboquant.broker.account.summary().log()
    feed.disconnect()
}


fun main() {

    when ("BROKER2") {
        "BROKER" -> ibkrBroker()
        "BROKER2" -> ibkrBroker2()
        "FEED" -> ibkrFeed()
        "BROKER_FEED" -> ibkrBrokerFeed()
        "HISTORIC" -> ibkrHistoricFeed()
    }

}