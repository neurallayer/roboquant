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
import org.roboquant.brokers.fee
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.jupyter.Chart
import org.roboquant.jupyter.PriceBarChart
import org.roboquant.logging.LastEntryLogger
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.toDoubleArray
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.PNL
import org.roboquant.metrics.ProgressMetric
import org.roboquant.policies.BettingAgainstBeta
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.*
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

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
    val feed = CSVFeed("data/US", CSVConfig(priceAdjust = true))
    val template = Asset("TEMPLATE", currencyCode = "EUR")
    val feed2 = CSVFeed("data/EU", CSVConfig(priceAdjust = true, template = template))
    feed.merge(feed2)

    val euro = Currency.getInstance("EUR")
    val usd = Currency.getInstance("USD")
    val currencyConverter = FixedExchangeRates(usd, euro to 1.2)
    Config.exchangeRates = currencyConverter

    val cash = Wallet(100_000.USD)
    val broker = SimBroker(cash)

    val strategy = EMACrossover.EMA_12_26
    val policy = DefaultPolicy(minAmount = 1_000.0, maxAmount = 15_000.0)

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
            roboquant.run(feed, runName = "run $fast-$slow")
        }
    }
    val maxEntry = logger.getMetric("account.equity").maxByOrNull { it.value }!!
    println(maxEntry.info.run)
}

suspend fun walkforwardParallel() {
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

fun trendFollowing2() {
    val feed = AvroFeed.sp500()
    val strategy = TAStrategy(200)

    strategy.buy {
        ta.recordHigh(it.high, 200) || ta.recordHigh(it.high, 50)
    }

    strategy.sell {
        ta.recordLow(it.low, 25)
    }

    val roboquant = Roboquant(strategy, ProgressMetric())
    roboquant.run(feed)
    roboquant.broker.account.summary().log()
    roboquant.broker.account.trades.summary().log()
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
    val feed = CSVFeed("/data/assets/us-stocks/Stocks", CSVConfig(".us.txt"))
    val market = CSVFeed("/data/assets/us-stocks/ETFs", CSVConfig(".us.txt", "spy.us.txt"))
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

fun svg() {
    val f = RandomWalk.lastYears(1, 1, generateBars = true)
    val asset = f.assets.first()
    val chart = PriceBarChart(f, asset)
    Chart.theme = "dark"

    val ssr = chart.asSSR(800)
    assertTrue(ssr.isNotEmpty())
    val svg = chart.asSVG()
    println(svg)

    val msg = MimeMessage()
    val session = msg.getSession()
    msg.send(
        "peter@neurallayer.com",
        session,
        "<html><body>The following chart the prices of ${asset.symbol} <img src='cid:ChartImage' /></body></html>",
        svg
    )
}

suspend fun main() {
    // Logging.setDefaultLevel(Level.FINE)
    Config.printInfo()

    when ("SVG") {
        "BETA" -> beta()
        "BETA2" -> beta2()
        "MULTI_RUN" -> multiRun()
        "WALKFORWARD_PARALLEL" -> println(measureTimeMillis { walkforwardParallel() })
        "MC" -> multiCurrency()
        "TESTING" -> testingStrategies()
        "TREND2" -> trendFollowing2()
        "VOLATILITY" -> volatility()
        "SVG" -> svg()

    }

}