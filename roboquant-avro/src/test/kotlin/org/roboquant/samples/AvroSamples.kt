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

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.avro.AvroFeed
import org.roboquant.brokers.Account
import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.PriceItemType
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.feeds.csv.PriceBarParser
import org.roboquant.feeds.csv.TimeParser
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.orders.Order
import org.roboquant.policies.BasePolicy
import org.roboquant.policies.FlexPolicy
import org.roboquant.policies.SignalResolution
import org.roboquant.policies.resolve
import org.roboquant.run
import org.roboquant.strategies.CombinedStrategy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.Signal
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.div
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AvroSamples {

    @Test
    @Ignore
    internal fun quotes_hft() = runBlocking {
        val tf = Timeframe.parse("2021-01-01T16:30:00Z", "2021-01-01T23:00:00Z", true)
        val f = RandomWalkFeed(tf, 10.millis, nAssets = 1, priceType = PriceItemType.QUOTE)
        val feed = AvroFeed("/tmp/test.avro")
        feed.record(f, compress = false)
        assertTrue(feed.exists())
        assertEquals(tf, feed.timeframe)

        feed.timeframe.sample(1.minutes, 5).forEach {
            val account = run(feed, EMAStrategy(), timeframe = it)
            println("$it => ${account.equityAmount}")
        }

        val channel = EventChannel()
        feed.playBackground(channel)
        var cnt = 0
        val start = Instant.now().toEpochMilli()
        try {
            while (true) {
                val evt = channel.receive()
                cnt += evt.items.size
            }
        } catch (_: ClosedReceiveChannelException) {
            // On purpose left empty, expected exception

        } finally {
            println(cnt)
            println(Instant.now().toEpochMilli() - start)
        }

    }

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

        val roboquant = Roboquant(strategy, policy = policy, broker = broker)
        val account = roboquant.run(feed)
        println(account.openOrders)
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
            roboquant.run(feed, timeframe = it)
        }

        // Walk forward learning
        feed.split(2.years).map { it.splitTwoWay(0.2) }.forEach { (train, test) ->
            roboquant.run(feed, timeframe = train)
            roboquant.reset()
            roboquant.run(feed, timeframe = test)
        }

    }

    @Test
    @Ignore
    internal fun signalsOnly() {
        class MyPolicy : BasePolicy() {

            override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
                return emptyList()
            }

        }

        val feed = AvroFeed("/tmp/us_full_v3.0.avro")

        val strategy = CombinedStrategy(
            EMAStrategy.PERIODS_50_200,
            EMAStrategy.PERIODS_12_26
        )

        val policy = MyPolicy().resolve(SignalResolution.NO_CONFLICTS)

        val roboquant = Roboquant(strategy, policy = policy)
        roboquant.run(feed, timeframe = Timeframe.past(5.years))
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
                        val rq = Roboquant(EMAStrategy())
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
        val roboquant = Roboquant(strategy, policy = policy, broker = broker)
        val account = roboquant.run(feed)

        println(account)
    }




}

