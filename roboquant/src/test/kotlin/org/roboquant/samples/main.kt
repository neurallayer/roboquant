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
import org.roboquant.brokers.Account
import org.roboquant.brokers.ECBExchangeRates
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.NoCostPricingEngine
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.AvroFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.filter
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.orders.Order
import org.roboquant.orders.summary
import org.roboquant.policies.BasePolicy
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.SignalResolution
import org.roboquant.policies.resolve
import org.roboquant.strategies.CombinedStrategy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

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

    val strategy = EMAStrategy.PERIODS_12_26
    val policy = FlexPolicy(orderPercentage = 0.02)

    val roboquant = Roboquant(strategy, AccountMetric(), policy = policy, broker = broker, logger = MemoryLogger())
    roboquant.run(feed)
    println(broker.account.openOrders.summary())
}

fun multiRun() {
    val feed = AvroFeed.sp500()
    val logger = LastEntryLogger()

    for (fast in 10..20..2) {
        for (slow in fast * 2..fast * 4..4) {
            val strategy = EMAStrategy(fast, slow)
            val roboquant = Roboquant(strategy, AccountMetric(), logger = logger)
            roboquant.run(feed, name = "run $fast-$slow")
        }
    }
    val maxEntry = logger.getMetric("account.equity").flatten().max()
    println(maxEntry)
}

suspend fun walkForwardParallel() {
    val feed = AvroFeed.sp500()
    val logger = LastEntryLogger()
    val jobs = ParallelJobs()

    feed.timeframe.split(2.years).forEach {
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy, AccountMetric(), logger = logger)
        jobs.add {
            roboquant.runAsync(feed, name = "run-$it")
        }
    }

    jobs.joinAll() // Make sure we wait for all jobs to finish
    val avgEquity = logger.getMetric("account.equity").flatten().average()
    println(avgEquity)
}


fun testingStrategies() {
    val strategy = EMAStrategy()
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
        roboquant.run(feed, train, test)
    }

}


fun signalsOnly() {
    class MyPolicy : BasePolicy(recording = true, prefix = "") {

        override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
            for (signal in signals) {
                record("signal.${signal.asset.symbol}", signal.rating.value)
            }
            return emptyList()
        }

    }

    val feed = AvroFeed("/tmp/us_full_v3.0.avro")
    val logger = MemoryLogger()

    val strategy = CombinedStrategy(
        EMAStrategy.PERIODS_50_200,
        EMAStrategy.PERIODS_12_26
    )

    val policy = MyPolicy().resolve(SignalResolution.NO_CONFLICTS)

    val roboquant = Roboquant(strategy, policy = policy, logger = logger)
    roboquant.run(feed, Timeframe.past(5.years))
}


fun csv2Avro(pathStr: String = "path", onlySP500: Boolean = true) {

    val path = Path(pathStr)
    val feed = CSVFeed.fromStooq((path / "nasdaq stocks").toString())
    val tmp = CSVFeed.fromStooq((path / "nyse stocks").toString())
    feed.merge(tmp)

    val sp500File = "/tmp/pricebars.avro"

    val symbols = Universe.sp500.getAssets(Instant.now()).symbols
    val filter = if (onlySP500) AssetFilter.includeSymbols(*symbols) else AssetFilter.all()

    AvroFeed.record(
        feed,
        sp500File,
        assetFilter = filter
    )

    val avroFeed = AvroFeed(sp500File)
    println("assets=${avroFeed.assets.size} timeframe=${avroFeed.timeframe}")

}

fun simple() {
    val strategy = EMAStrategy()
    val feed = AvroFeed.sp500()
    val roboquant = Roboquant(strategy)
    roboquant.run(feed)
    println(roboquant.broker.account.fullSummary())
}

fun forexFeed() {
    val path = System.getProperty("user.home") + "/data/forex/"
    val feed = CSVFeed.fromHistData(path)

    println("timeframe=${feed.timeframe} symbols=${feed.assets.symbols.toList()}")
    Config.exchangeRates = ECBExchangeRates.fromWeb()
    val strategy = EMAStrategy()
    val rq = Roboquant(strategy, AccountMetric())
    rq.run(feed)
    println(rq.broker.account.summary())
}


fun forexRun() {
    val feed = AvroFeed.forex()
    Currency.increaseDigits(3)
    val rq = Roboquant(EMAStrategy(), AccountMetric(), broker = SimBroker(pricingEngine = NoCostPricingEngine()))
    rq.run(feed, timeframe = Timeframe.parse("2022-01-03", "2022-02-10"))

    for (trade in rq.broker.account.trades) {
        val tf = Timeframe(trade.time, trade.time, true)
        val pricebar = feed.filter<PriceAction>(timeframe = tf).firstOrNull { it.second.asset == trade.asset }
        if (pricebar == null) {
            println(trade)
            println(feed.filter<PriceAction>(timeframe = tf))
            throw RoboquantException("couldn't find trade action")
        } else {
            assertEquals(pricebar.second.getPrice(), trade.price)
        }
    }
}


fun profileTest() {
    val feed = AvroFeed.sp500()
    val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = SilentLogger())
    rq.run(feed)
}


fun performanceTest() {
    val feed = AvroFeed(Config.home / "all_1962_2023.avro")
    repeat(3) {
        val t = measureTimeMillis {
            val jobs = ParallelJobs()
            repeat(4) {
                jobs.add {
                    val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = SilentLogger())
                    rq.run(feed)
                }
            }
            jobs.joinAllBlocking()
        }
        println(t)
    }
}


fun cfd() {

    val feed = CSVFeed("/tmp/DE40CASH.csv") {
        template = Asset("TEMPLATE", AssetType.CFD, Currency.EUR, Exchange.DEX)
    }
    require(feed.assets.size == 1)

    val strategy = EMAStrategy()

    // We don't want the default initial deposit in USD, since then we need a currency convertor
    val initialDeposit = 10_000.EUR.toWallet()

    // We configure a large leverage of 50x, so we 10_000 x 50 = 500_000K buyingpower to start with
    val leverage = 50.0
    val broker = SimBroker(initialDeposit, accountModel = MarginAccount(leverage = leverage))

    // Since we only trade in 1 asset and have a lot of leverage, we allow up to 1000% of our equity (10k) allocated to
    // one order
    val orderPercentage = 10.0
    val policy = FlexPolicy(shorting = true, orderPercentage = orderPercentage, safetyMargin = 0.5)
    val roboquant = Roboquant(strategy, AccountMetric(), broker = broker, policy = policy)
    roboquant.run(feed)

    val account = roboquant.broker.account
    println(account.summary())
    println(account.trades.summary())
}


suspend fun main() {
    Config.printInfo()

    when ("PERFORMANCE") {
        "SIMPLE" -> simple()
        "CSV2AVRO" -> csv2Avro("/tmp/daily/us", false)
        "MULTI_RUN" -> multiRun()
        "WALKFORWARD_PARALLEL" -> println(measureTimeMillis { walkForwardParallel() })
        "MC" -> multiCurrency()
        "TESTING" -> testingStrategies()
        "SIGNALS" -> signalsOnly()
        "FOREX" -> forexFeed()
        "FOREX_RUN" -> forexRun()
        "PROFILE" -> profileTest()
        "PERFORMANCE" -> performanceTest()
        "CFD" -> cfd()
    }

}