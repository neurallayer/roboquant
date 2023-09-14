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

import kotlinx.coroutines.*
import org.roboquant.Roboquant
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.brokers.summary
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.TradePrice
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.csv.PriceBarParser
import org.roboquant.feeds.csv.TimeParser
import org.roboquant.loggers.ConsoleLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.summary
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun multiCurrency() {
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
        roboquant.run(feed, train)
        roboquant.reset(false)
        roboquant.run(feed, test)
    }

}


fun cfd() {
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


fun largeTest() {

    class MyLiveFeed : LiveFeed() {

        fun start(delayInMillis: Long) {
            val scope = CoroutineScope(Dispatchers.Default + Job())

            scope.launch {
                val asset = Asset("ABC")
                val actions = listOf(TradePrice(asset, 100.0))

                while (true) {
                    try {
                        send(event = Event(actions, Instant.now()))
                        delay(delayInMillis)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }

        }

    }

    val feed = MyLiveFeed()
    val tf = Timeframe.next(20.seconds)

    val jobs = ParallelJobs()

    var run = 0
    tf.sample(2.seconds, 100).forEach {
        val name = "run-${run++}"
        jobs.add {
            val rq = Roboquant(EMAStrategy(), ProgressMetric(), logger = ConsoleLogger())
            rq.runAsync(feed, it, name = name)
            // val actions = rq.logger.getMetric("progress.actions", name).values.first()
            // println("$actions $name $it")
            // assertTrue(actions > 30)
        }
        println("run $name added")
    }

    feed.start(delayInMillis = 50)
    println("feed started")

    jobs.joinAllBlocking()
    println("runs are done")
}


fun main() {
    Config.printInfo()

    when ("LARGE") {
        "MC" -> multiCurrency()
        "TESTING" -> testingStrategies()
        "CFD" -> cfd()
        "LARGE" -> largeTest()
    }

}
