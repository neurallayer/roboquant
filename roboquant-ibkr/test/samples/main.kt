package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRFeed
import org.roboquant.logging.MemoryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.PriceMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover

fun ibkrBroker() {
    val feed = CSVFeed("data/US", CSVConfig(priceAdjust = true))
    val currencyConverter = FixedExchangeRates(Currency.USD, Currency.EUR to 1.2)
    val broker = IBKRBroker(currencyConverter = currencyConverter)

    val strategy = EMACrossover.midTerm()

    val roboquant = Roboquant(strategy, AccountSummary(), broker = broker)
    val tf = TimeFrame.nextMinutes(3)
    roboquant.run(feed, tf)
    broker.account.summary()
    broker.disconnect()
}


fun ibkrBrokerFeed() {


    val currencyConverter = FixedExchangeRates(Currency.EUR, Currency.USD to 0.89)
    val broker = IBKRBroker(currencyConverter = currencyConverter)
    broker.connect(port = 4002)

    // Subscribe to all assets in the portfolio
    val feed = IBKRFeed(port = 4002)
    val assets = broker.account.portfolio.assets
    // val asset = Asset("ABN", AssetType.STK, "EUR", "AEB")
    println(assets.first().serialize())
    println(assets.last().serialize())
    feed.subscribe(*assets.toTypedArray())

    val strategy = EMACrossover.shortTerm()

    val roboquant = Roboquant(strategy, AccountSummary(), ProgressMetric(), broker = broker)
    val tf = TimeFrame.nextMinutes(1)
    roboquant.run(feed, tf)
    broker.account.summary().log()
    roboquant.logger.summary().log()
    broker.disconnect()
    feed.disconnect()
}


fun ibkrFeed() {
    val feed = IBKRFeed()
    val asset = Asset("ABN", AssetType.STOCK, "EUR", "AEB")
    feed.subscribe(asset)

    val cash = Cash(Currency.EUR to 1_000_000.0)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.shortTerm()
    val roboquant =
        Roboquant(strategy, AccountSummary(), ProgressMetric(), PriceMetric(asset), broker = broker)
    val tf = TimeFrame.nextMinutes(10)

    roboquant.run(feed, tf)
    feed.disconnect()
    roboquant.logger.summary().log()
}


fun main() {

    when("FEED") {
        "BROKER" -> ibkrBroker()
        "FEED" -> ibkrFeed()
        "BROKER_FEED" -> ibkrBrokerFeed()
    }

}