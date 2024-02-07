/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.samples

import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.brokers.Account
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.NoCostPricingEngine
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.csv.CSVConfig
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.csv.PriceBarParser
import org.roboquant.feeds.csv.TimeParser
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.loggers.latestRun
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.AlphaBetaMetric
import org.roboquant.orders.Order
import org.roboquant.orders.summary
import org.roboquant.policies.BasePolicy
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.SignalResolution
import org.roboquant.policies.resolve
import org.roboquant.strategies.CombinedStrategy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.Signal
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AvroSamples {

    @Test
    @Ignore
    internal fun multiCurrency() {
        val feed = CSVFeed("data/US") {
            priceParser = PriceBarParser(priceAdjust = true)
        }
        val feed2 = CSVFeed("data/EU") {
            priceParser = PriceBarParser(priceAdjust = true)
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
        val policy = FlexPolicy {
            orderPercentage = 0.02
        }

        val roboquant = Roboquant(strategy, AccountMetric(), policy = policy, broker = broker, logger = MemoryLogger())
        roboquant.run(feed)
        println(broker.account.openOrders.summary())
    }

    @Test
    @Ignore
    internal fun multiRun() {
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

    @Test
    @Ignore
    internal fun walkForwardParallel() = runBlocking {
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

    @Test
    @Ignore
    internal fun testingStrategies() {
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
        feed.split(2.years).map { it.splitTwoWay(0.2) }.forEach { (train, test) ->
            roboquant.run(feed, train)
            roboquant.reset(false)
            roboquant.run(feed, test)
        }

    }

    @Test
    @Ignore
    internal fun signalsOnly() {
        class MyPolicy : BasePolicy(prefix = "") {

            init {
                enableMetrics = true
            }

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

    @Test
    @Ignore
    internal fun simple() {
        val strategy = EMAStrategy()
        val feed = AvroFeed.sp500()
        val roboquant = Roboquant(strategy)
        roboquant.run(feed)
        println(roboquant.broker.account.fullSummary())
    }

    @Test
    @Ignore
    internal fun aggregator() {
        val forex = AvroFeed.forex()
        val feed = AggregatorFeed(forex, 15.minutes)
        feed.apply<PriceAction> { _, time ->
            println(time)
        }
    }

    @Test
    @Ignore
    internal fun forexRun() {
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

    @Test
    @Ignore
    internal fun profileTest() {
        val feed = AvroFeed.sp500()
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = SilentLogger())
        rq.run(feed)
    }

    @Test
    @Ignore
    internal fun performanceTest() {
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

    @Test
    @Ignore
    internal fun cfd() {
        val dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")

        fun parse(line: List<String>, asset: Asset): Instant {
            val text = line[0] + " " + line[1]
            val dt = LocalDateTime.parse(text, dtf)
            return asset.exchange.getInstant(dt)
        }

        val feed = CSVFeed("/tmp/DE40CASH.csv") {
            template = Asset("TEMPLATE", AssetType.CFD, Currency.EUR, Exchange.DEX)
            separator = '\t'
            timeParser = TimeParser { a, b -> parse(a, b) }
        }

        require(feed.assets.size == 1)

        val strategy = EMAStrategy()

        // We don't want the default initial deposit in USD, since then we need a currency convertor
        val initialDeposit = 10_000.EUR.toWallet()

        // We configure a large leverage of 50x, so we 10_000 x 50 = 500_000K buyingpower to start with
        val leverage = 50.0
        val broker = SimBroker(initialDeposit, accountModel = MarginAccount(leverage = leverage))

        // Since we only trade in one asset and have leverage, we allow up to 1000% of our equity (10k) allocated to
        // one order
        val policy = FlexPolicy.capitalBased {
            shorting = true
            orderPercentage = 90.percent
            safetyMargin = 10.percent
        }
        val roboquant = Roboquant(strategy, AccountMetric(), broker = broker, policy = policy)
        roboquant.run(feed)

        val account = roboquant.broker.account
        println(account.summary())
        println(account.trades.summary())
    }

    @Test
    @Ignore
    internal fun feedRecorder() {
        val tf = Timeframe.past(1.years)
        val symbol = "BTCBUSD"
        val template = Asset(symbol, AssetType.CRYPTO, currency = Currency.getInstance("BUSD"))
        val feed = RandomWalkFeed(tf, 1.seconds, template = template, nAssets = 1, generateBars = false)
        val fileName = "/tmp/${symbol}-1sec.avro"
        val t = measureTimeMillis {
            AvroFeed.record(feed, fileName)
        }
        val f = File(fileName)
        println(t)
        println(f.length() / 1_000_000)

        val t2 = measureTimeMillis {
            AvroFeed(fileName)
        }
        println(t2)
    }

    @Test
    @Ignore
    internal fun generateDemoFeed() {

        val pathStr = Config.getProperty("datadir", "/tmp/us")

        val timeframe = Timeframe.fromYears(2014, 2024)
        val symbols = Universe.sp500.getAssets(timeframe.end).map { it.symbol }.toTypedArray()
        assertTrue(symbols.size > 490)

        val config = CSVConfig.stooq()
        val path = Path(pathStr)
        val path1 = path / "nasdaq stocks"
        val path2 = path / "nyse stocks"

        val feed = CSVFeed(path1.toString(), config)
        val tmp = CSVFeed(path2.toString(), config)
        feed.merge(tmp)

        val sp500File = "/tmp/sp500_pricebar_v6.1.avro"

        AvroFeed.record(
            feed,
            sp500File,
            true,
            timeframe,
            assetFilter = AssetFilter.includeSymbols(*symbols)
        )

        // Some basic sanity checks that recording went ok
        val avroFeed = AvroFeed(sp500File)
        assertTrue(avroFeed.assets.size > 490)
        assertTrue(avroFeed.assets.symbols.contains("AAPL"))
        assertTrue(avroFeed.timeframe > 4.years)

    }

    @Test
    @Ignore
    fun alpha() {
        val feed = AvroFeed.sp500()
        val rq = Roboquant(EMAStrategy(), AlphaBetaMetric(250))
        rq.run(feed)
        val alpha = rq.logger.getMetric("account.alpha").latestRun()
        val beta = rq.logger.getMetric("account.beta").latestRun()
        println("alpha is ${alpha.last()}")
        println("beta is ${beta.last()}")
    }
}

