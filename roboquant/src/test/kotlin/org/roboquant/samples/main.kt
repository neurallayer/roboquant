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
import org.roboquant.brokers.fee
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.filter
import org.roboquant.feeds.timeseries
import org.roboquant.logging.LastEntryLogger
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.toDoubleArray
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.PNL
import org.roboquant.metrics.ProgressMetric
import org.roboquant.policies.BettingAgainstBeta
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.NoSignalStrategy
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

fun volatility() {
    val feed = AvroFeed.sp500()
    val data = feed.filter<PriceBar> { it.asset.symbol == "IBM" }
    val arr = data.map { it.second.close }.toDoubleArray()
    val returns = arr.returns()
    println(returns.variance())
    println(returns.filter { it > 0.0 }.toDoubleArray().variance())
    println(returns.filter { it < 0.0 }.toDoubleArray().variance())
}

fun multiCurrency() {
    val feed = CSVFeed("data/US") {
        priceAdjust = true
    }
    val feed2 = CSVFeed("data/EU") {
        priceAdjust = true
        template = Asset("TEMPLATE", currencyCode = "EUR")
    }
    feed.merge(feed2)

    val euro = Currency.getInstance("EUR")
    val usd = Currency.getInstance("USD")
    val currencyConverter = FixedExchangeRates(usd, euro to 1.2)
    Config.exchangeRates = currencyConverter

    val cash = Wallet(100_000.USD)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.EMA_12_26
    val policy = DefaultPolicy(orderPercentage = 0.02)

    val roboquant = Roboquant(strategy, AccountSummary(), policy = policy, broker = broker, logger = MemoryLogger())
    roboquant.run(feed)
    broker.account.openOrders.summary().print()
}

fun multiRun() {
    val feed = AvroFeed.sp500()
    val logger = LastEntryLogger()

    for (fast in 10..20..2) {
        for (slow in fast * 2..fast * 4..4) {
            val strategy = EMACrossover(fast, slow)
            val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
            roboquant.run(feed, name = "run $fast-$slow")
        }
    }
    val maxEntry = logger.getMetric("account.equity").max()
    println(maxEntry.info.run)
}

suspend fun walkForwardParallel() {
    val feed = AvroFeed.sp500()
    val logger = LastEntryLogger()
    val jobs = ParallelJobs()

    feed.timeframe.split(2.years).forEach {
        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
        jobs.add {
            roboquant.runAsync(feed, runName = "run-$it")
        }
    }

    jobs.joinAll() // Make sure we wait for all jobs to finish
    val avgEquity = logger.getMetric("account.equity").toDoubleArray().mean()
    println(avgEquity)
}

fun testingStrategies() {
    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy)
    val feed = CSVFeed("data/US")

    // Basic use case
    roboquant.run(feed)

    // Walk forward
    feed.split(2.years).forEach {
        roboquant.run(feed, it)
    }

    // Walk forward learning
    feed.split(2.years).map { it.splitTrainTest(0.2) }.forEach { (train, test) ->
        roboquant.run(feed, train, test, episodes = 100)
    }

}

fun calcCorrelation() {
    val feed = AvroFeed.sp500()
    val data = feed.filter<PriceBar>(Timeframe.coronaCrash2020)
    val timeseries = data.timeseries()
    val result = timeseries.correlation()

    val mostUncorrelated = result.toList().sortedBy { it.second.absoluteValue }.take(50)
    for ((assets, corr) in mostUncorrelated) {
        println("${assets.first.symbol} ${assets.second.symbol} = $corr")
    }

}

fun beta() {
    val feed = CSVFeed("/data/assets/stock-market/stocks/")
    val market = CSVFeed("/data/assets/stock-market/market/")
    feed.merge(market)
    val strategy = NoSignalStrategy()
    val marketAsset = feed.find("SPY")

    val policy = BettingAgainstBeta(feed.assets, marketAsset, maxAssetsInPortfolio = 10)
    policy.recording = true
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), policy = policy, logger = logger)
    roboquant.run(feed)
    logger.summary().print()
    roboquant.broker.account.summary().print()

}

fun beta2() {
    val feed = CSVFeed("/data/assets/us-stocks/Stocks") {
        fileExtension = ".us.txt"
    }
    val market = CSVFeed("/data/assets/us-stocks/ETFs") {
        fileExtension = ".us.txt"
        filePattern = "spy.us.txt"

    }
    feed.merge(market)
    val strategy = NoSignalStrategy()
    val marketAsset = feed.find("SPY")
    val policy = BettingAgainstBeta(feed.assets, marketAsset, 60, maxAssetsInPortfolio = 10)
    policy.recording = true
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), PNL(), policy = policy, logger = logger)
    roboquant.run(feed)
    logger.summary().print()
    println(roboquant.broker.account.summary())
    println(roboquant.broker.account.trades.fee)

}

suspend fun main() {
    // Logging.setDefaultLevel(Level.FINE)
    Config.printInfo()

    when ("CORR") {
        "BETA" -> beta()
        "CORR" -> calcCorrelation()
        "BETA2" -> beta2()
        "MULTI_RUN" -> multiRun()
        "WALKFORWARD_PARALLEL" -> println(measureTimeMillis { walkForwardParallel() })
        "MC" -> multiCurrency()
        "TESTING" -> testingStrategies()
        "VOLATILITY" -> volatility()
    }

}