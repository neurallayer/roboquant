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

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.roboquant.Roboquant
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.fee
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.avro.AvroUtil
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.LastEntryLogger
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.toDoubleArray
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.PNL
import org.roboquant.metrics.ProgressMetric
import org.roboquant.policies.BettingAgainstBeta
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.*
import java.nio.file.Files
import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.system.measureTimeMillis


fun large5() {
    val feed = AvroFeed.sp500()
    val strategy = EMACrossover.longTerm()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
    val t = measureTimeMillis {
        roboquant.run(feed)
    }
    roboquant.broker.account.summary().print()
    println(t)
}


fun large6() {

    val dataHome = Path("/Users/peter/data")

    fun getConfig(exchange: String): CSVConfig {
        Exchange.getInstance(exchange)
        return CSVConfig(
            fileExtension = ".us.txt",
            parsePattern = "??T?OHLCV?",
            template = Asset("TEMPLATE", exchangeCode = exchange)
        )
    }

    val config1 = getConfig("NASDAQ")
    val config2 = getConfig("NYSE")
    val path = dataHome / "stooq/daily/us/"
    var feed: CSVFeed? = null

    for (d in Files.list(path)) {
        if (d.name.startsWith("nasdaq stocks")) {
            val tmp = CSVFeed(d.toString(), config1)
            if (feed === null) feed = tmp else feed.merge(tmp)
        }
    }

    for (d in Files.list(path)) {
        if (d.name.startsWith("nyse stocks")) {
            val tmp = CSVFeed(d.toString(), config2)
            if (feed === null) feed = tmp else feed.merge(tmp)
        }
    }

    // val avroPath = dataHome / "avro/us_2000_2021.avro"
    // AvroUtil.record(feed!!, avroPath.toString(), TimeFrame.fromYears(2000, 2021), 6)
    val avroPath = dataHome / "avro/us_stocks.avro"
    AvroUtil.record(feed!!, avroPath.toString(), Timeframe.fromYears(1900, 2021), compressionLevel = 6)

}



fun small6() {

    val dataHome = Path("/Users/peter/data")

    fun getConfig(exchange: String): CSVConfig {
        Exchange.getInstance(exchange)
        return CSVConfig(
            fileExtension = ".us.txt",
            parsePattern = "??T?OHLCV?",
            template = Asset("TEMPLATE", exchangeCode = exchange)
        )
    }

    val config1 = getConfig("NASDAQ")
    val config2 = getConfig("NYSE")
    val path = dataHome / "stooq/daily/us2/"
    var feed: CSVFeed? = null

    for (d in Files.list(path)) {
        if (d.name.startsWith("nasdaq stocks")) {
            val tmp = CSVFeed(d.toString(), config1)
            if (feed === null) feed = tmp else feed.merge(tmp)
        }
    }

    for (d in Files.list(path)) {
        if (d.name.startsWith("nyse stocks")) {
            val tmp = CSVFeed(d.toString(), config2)
            if (feed === null) feed = tmp else feed.merge(tmp)
        }
    }

    // val avroPath = dataHome / "avro/us_2000_2021.avro"
    // AvroUtil.record(feed!!, avroPath.toString(), TimeFrame.fromYears(2000, 2021), 6)
    val avroPath = dataHome / "avro/us_stocks_small.avro"
    AvroUtil.record(feed!!, avroPath.toString(), Timeframe.fromYears(1900, 2021), compressionLevel = 6)

}

fun largeRead() {
    val dataHome = Path("/Users/peter/data")
    val avroPath = dataHome / "avro/us_stocks.avro"


    val t = measureTimeMillis {
        val feed = AvroFeed(avroPath.toString(), useIndex = false)
        val strategy = EMACrossover()

        val roboquant = Roboquant(strategy, AccountSummary())
        roboquant.run(feed)
        // val broker = roboquant.broker as SimBroker
        // broker.liquidatePortfolio()
        roboquant.broker.account.summary().log()
    }
    println(t)



}

fun twoMillionBars() {
    val timeline = mutableListOf<Instant>()
    var start = Instant.parse("2000-01-01T09:00:00Z")

    // Create a timeline of 1 million entries
    repeat(20_000) {
        timeline.add(start)
        start += 1.minutes
    }

    // Create a random walk for the timeline provided, totol 2_000_000 candle sticks
    val feed = RandomWalk(timeline, 100)

    // Create a roboquant using Exponential Weighted Moving Average
    val strategy = EMACrossover()
    val roboquant = Roboquant(strategy, ProgressMetric())

    // Measure how long it takes to run a back-test over 2 million candlesticks
    val time = measureTimeMillis {  roboquant.run(feed) }
    println("Time taken to run a full back-test over 2M Candlesticks is $time milliseconds")
}

fun volatility() {
    val feed = AvroFeed.sp500()
    val data = feed.filter<PriceBar> { it.asset.symbol == "IBM" }
    val arr = data.map { it.second.close }.toDoubleArray()
    val returns =  arr.returns()
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

    val strategy = EMACrossover.midTerm()
    val policy = DefaultPolicy(minAmount = 1_000.0, maxAmount = 15_000.0)

    val roboquant = Roboquant(strategy, AccountSummary(), policy = policy, broker = broker, logger = MemoryLogger())
    roboquant.run(feed)
    broker.account.summary().print()
}

fun manyMinutes() {
    val strategy = EMACrossover.longTerm()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)

    val tf = Timeframe.fromYears(2019, 2019)
    val timeline = tf.toMinutes(excludeWeekends = true)
    val feed = RandomWalk(timeline, 10, generateBars = true)

    roboquant.run(feed)
    logger.summary().print()
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
    val maxEntry = logger.getMetric("account.equity").maxByOrNull{ it.value }!!
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


suspend fun multiRunParallel() {
    val feed = AvroFeed.sp500()
    val logger = LastEntryLogger()
    val jobs = ParallelJobs()

    for (fast in 10..20..2) {
        for (slow in fast * 2..fast * 4..4) {
            val strategy = EMACrossover(fast, slow)
            val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
            jobs.add {
                roboquant.runAsync(feed, runName = "run $fast-$slow")
            }
        }
    }

    jobs.joinAll() // Make sure we wait for all jobs to finish
    val maxEntry = logger.getMetric("account.equity").maxByOrNull{ it.value }!!
    println(maxEntry.info.run)
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


suspend fun runParallel() {
    val feed = RandomWalk.lastDays(100, 10, false)
    val deferredList = mutableListOf<Deferred<MemoryLogger>>()
    for (i in 10..15) {
        for (j in i + 1..i + 5) {
            val s = EMACrossover(i, j)
            val logger = MemoryLogger(false)
            val e = Roboquant(s, AccountSummary(), logger = logger)
            val deferred = Background.async {
                e.runAsync(feed, runName = "Run $i $j")
                logger
            }

            deferredList.add(deferred)
        }
    }

    val loggers = deferredList.awaitAll()
    val l = Logging.getLogger("ParallelRuns")
    loggers.forEach {
        val entry = it.getMetric("account.value").last()
        l.info { "${entry.info.run}  ${entry.value}" }
    }

}

fun ta() {
    val shortTerm = 30
    val longTerm = 50
    val strategy = TAStrategy(longTerm)

    strategy.buy { price ->
        val emaShort = ta.ema(price.close, shortTerm)
        emaShort > ta.ema(price.close, longTerm) && ta.cdlMorningStar(price)
    }

    strategy.sell { price ->
        ta.cdl3BlackCrows(price) || (ta.cdl2Crows(price) && ta.ema(price.close, shortTerm) < ta.ema(
            price.close,
            longTerm
        ))
    }

    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
    val feed = CSVFeed("data/US")
    roboquant.run(feed)
    logger.summary(10).print()
}


fun taLarge() {
    val shortTerm = 30
    val longTerm = 50
    val strategy = TAStrategy(longTerm)

    strategy.buy { price ->
        with(ta) {
            cdlMorningStar(price) || cdl3Inside(price) || cdl3Outside(price) || cdl3LineStrike(price) ||
                    cdlHammer(price) || cdlPiercing(price) || cdlSpinningTop(price) || cdlRiseFall3Methods(price) ||
                    cdl3StarsInSouth(price) || cdlOnNeck(price)
        }
    }

    strategy.sell { price ->
        ta.cdl3BlackCrows(price) || (ta.cdl2Crows(price) && ta.ema(price.close, shortTerm) < ta.ema(
            price.close,
            longTerm
        ))
    }

    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
    val feed = AvroFeed("/Users/peter/data/avro/us_2000_2020.avro")
    roboquant.run(feed)
    roboquant.broker.account.fullSummary().print()
}

fun avro() {
    val feed = AvroFeed("/data/assets/avro/universe.avro")
    val strategy = EMACrossover()
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)
    roboquant.run(feed, Timeframe.fromYears(1960, 2021))
    logger.summary().print()
}


fun avroGen() {
    // val feed = CSVFeed("/data/assets/stock-market/stocks/")
    val feed = CSVFeed("/Users/peter/data/individual_stocks_5yr", CSVConfig("_data.csv"))
    val t = measureTimeMillis {
        val file = "/Users/peter/tmp/5yr_sp500.avro"
        AvroUtil.record(feed, file)
    }
    println(t)
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


fun avroCapture() {
    val feed = CSVFeed("data/US")

    val avroFile = "/Users/peter/data/avro/tmp.avro"
    //val feed = CSVFeed("/data/assets/individual_stocks_5yr", CSVConfig("_data.csv"))
    AvroUtil.record(feed, avroFile)

    val feed2 = AvroFeed(avroFile, useIndex = true)
    val strategy = EMACrossover()

    val roboquant = Roboquant(strategy, ProgressMetric())
    roboquant.run(feed2)
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



suspend fun main() {
    // Logging.setDefaultLevel(Level.FINE)
    Config.printInfo()

    when ("ONE_MILLION") {
        // "CRYPTO" -> crypto()
        "BETA" -> beta()
        "BETA2" -> beta2()
        "LARGE5" -> large5()
        "LARGE6" -> large6()
        "SMALL6" -> small6()
        "LARGEREAD" -> largeRead()
        "MULTI_RUN" -> multiRun()
        "MULTI_RUN_PARALLEL" -> println( measureTimeMillis {  multiRunParallel() })
        "WALKFORWARD_PARALLEL" -> println( measureTimeMillis {  walkforwardParallel() })
        "ONE_MILLION" -> twoMillionBars()
        "MC" -> multiCurrency()
        "MINUTES" -> manyMinutes()
        "TESTING" -> testingStrategies()
        "TA" -> ta()
        "TREND2" -> trendFollowing2()
        "TA_LARGE" -> taLarge()
        "PARALLEL" -> runParallel()
        "AVRO" -> avro()
        "AVRO_GEN" -> avroGen()
        "AVRO_CAP" -> avroCapture()
        "VOLATILITY" -> volatility()
        "AVRO_ALL" -> {
            avroGen(); avro()
        }

    }

}