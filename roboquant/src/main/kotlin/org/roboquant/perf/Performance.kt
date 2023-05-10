/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.perf


import org.roboquant.Roboquant
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PNLMetric
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.CombinedStrategy
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import java.time.Instant
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

/**
 * Simple fast strategy
 */
private class FastStrategy(private val skip: Int) : Strategy {

    var steps = 0
    var buy = true

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for (asset in event.prices.keys) {
            steps++
            if ((steps % (skip + 1)) == 0) {
                val rating = if (buy) Rating.BUY else Rating.SELL
                val signal = Signal(asset, rating)
                signals.add(signal)
                buy = !buy
            }
        }
        return signals
    }

}

/**
 * Fast feed
 */
private class FastFeed(val nAssets : Int, val events: Int) : Feed {

    private val assets = mutableListOf<Asset>()
    private val start = Instant.parse("2000-01-01T00:00:00Z")
    val size = nAssets * events

    init {
        repeat(nAssets) {  assets.add(Asset("TEST-$it")) }
    }

    override suspend fun play(channel: EventChannel) {
        repeat(events) {
            val actions = HashMap<Asset, PriceBar>(nAssets)
            // val actions = ArrayList<PriceBar>(nAssets)
            val open = 100.0 + 10 * (it / events)
            val data = doubleArrayOf(open,open+1.0,open-1.0,open, 500.0)
            for (asset in assets) {
                val action = PriceBar(asset, data)
                actions[asset] = action
                // actions.add(action)
            }
            channel.send(Event(actions, start + it.millis))
        }
    }

}

/**
 * Performance test that runs a number of back-tests scenarios against different feed sizes to measure performance
 * and detect possible performance regressions. Each test is run 3 times in order to minimize fluctuations cause by
 * outside events like virus scanners.
 *
 * The main purpose is to validate the performance, throughput and stability of the back test engine, not any particular
 * feed, strategy or metric. So the used feed and strategy are optimized for this test and not something you would
 * normally use.
 */
private object Performance {

    private const val skip = 999 // create signal in 1 out of 999 price-action
    private fun getStrategy(skip:Int) : Strategy = FastStrategy(skip)

    /**
     * Try to make the results more reproducible by running the code multiple times and take best time.
     */
    private fun measure(block: () -> Unit): Long {
        var best = Long.MAX_VALUE
        repeat(3) {
            System.gc()
            val t = measureTimeMillis(block)
            if (t < best) best = t
        }
        return best
    }

    /**
     * Basic test with minimal overhead
     */
    private fun seqRun(feed: FastFeed, backTests: Int): Pair<Long, Int> {
        var trades = 0
        val t = measure {
            // sequential
            trades = 0
            repeat(backTests) {
                val roboquant = Roboquant(getStrategy(skip), logger = SilentLogger())
                roboquant.run(feed)
                trades += roboquant.broker.account.trades.size
            }

        }
        return Pair(t, trades)
    }

    /**
     * Test iterating over the feed while filtering
     */
    private fun feedFilter(feed: FastFeed): Long {
        val asset = Asset("UNKNOWN")
        return measure {
            feed.filter<PriceBar> {
                it.asset == asset
            }
        }
    }

    /**
     * Test with 3 strategies, margin account, shorting, metrics and some logging overhead included
     */
    private fun extendedRun(feed: FastFeed): Long {
        return measure {

            val strategy = CombinedStrategy(
                getStrategy(skip -1),
                getStrategy(skip),
                getStrategy(skip +1),
            )

            val broker = SimBroker(accountModel = MarginAccount())
            val policy = FlexPolicy(shorting = true)

            val roboquant = Roboquant(
                strategy,
                AccountMetric(),
                PNLMetric(),
                broker = broker,
                policy = policy,
                logger = LastEntryLogger()
            )
            roboquant.run(feed)
        }
    }

    /**
     * Run parallel back tests
     */
    private fun parRun(feed: Feed, backTests: Int): Long {

        return measure {
            val jobs = ParallelJobs()
            repeat(backTests) {
                jobs.add {
                    val roboquant = Roboquant(getStrategy(skip), logger = SilentLogger(), channelCapacity = 3)
                    roboquant.runAsync(feed)
                }
            }
            jobs.joinAllBlocking()
        }
    }

    @Suppress("ImplicitDefaultLocale")
    fun test() {
        Config.printInfo()
        data class Combination(val events: Int, val assets: Int, val backTests: Int)
        val combinations = listOf(
            Combination(1_000, 10, 100),
            Combination(1_000, 50, 100),
            Combination(2_000, 50, 100),
            Combination(5_000, 100, 100),
            Combination(5_000, 200, 100),
            Combination(10_000, 500, 100),
            Combination(20_000, 500, 100)
        )

        val header = String.format("\n%8S %8S %8S %11S %8S %9S %16S %14S %8S %11S",
            "candles", "assets", "events", "backtests", "feed", "ext-run",
            "sequential-run","parallel-run", "trades", "candles/s"
            )

        println(header)
        println(" " + "━".repeat(header.length - 2))
        for ((events, assets, backTests) in combinations ) {
            // single run
            val feed = FastFeed(assets, events)
            val t1 = feedFilter(feed)
            val t2 = extendedRun(feed)

            // multi-run
            val (t3, trades) = seqRun(feed, backTests)
            val t4 = parRun(feed, backTests)

            val candles = assets*events*backTests/1_000_000
            val line = String.format("%7dM %8d %8d %11d %6dms %7dms %14dms %12dms %8d %10dM",
                    candles, assets, events, backTests, t1, t2, t3, t4, trades, feed.size * backTests / (t4 * 1000)
                )
            println(line)
        }
        println()
        exitProcess(0)
    }


}

/**
 * Run the performance test
 */
fun main() {
    Performance.test()
}