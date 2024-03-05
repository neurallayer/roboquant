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

package org.roboquant.perf

import org.roboquant.Roboquant
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PNLMetric
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.*
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

/**
 * Simple fast strategy.
 *
 * Not realistic, but with minimal overhead to ensure we can measure the performance of the engine and not the strategy.
 */
private class FastStrategy(private val skip: Int) : Strategy {

    var steps = 0
    var buy = true

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for (action in event.actions.filterIsInstance<PriceAction>()) {
            steps++
            if ((steps % (skip + 1)) == 0) {
                val signal = if (buy) Signal.buy(action.asset) else Signal.sell(action.asset)
                signals.add(signal)
                buy = !buy
            }
        }
        return signals
    }

}

/**
 * Fast feed.
 *
 * Not realistic, but with minimal overhead to ensure we can measure the performance of the engine and are not
 * impacted by the feed.
 */
private class FastFeed(nAssets: Int, val events: Int) : Feed {

    private val assets = mutableListOf<Asset>()
    private val start = Instant.parse("2000-01-01T00:00:00Z")
    val actions = ArrayList<PriceBar>(nAssets)
    val size = nAssets * events

    init {
        repeat(nAssets) { assets.add(Asset("TEST-$it")) }
        val data = doubleArrayOf(100.0, 101.0, 99.0, 100.0, 10000.0)
        for (asset in assets) {
            val action = PriceBar(asset, data)
            actions.add(action)
        }
    }

    override suspend fun play(channel: EventChannel) {
        repeat(events) {
            channel.send(Event(actions, start + it.millis))
        }
    }

}

/**
 * Performance test that runs a number of back-tests scenarios against different feed sizes to measure performance
 * and detect possible performance regressions.
 * Each test is run three times to minimize fluctuations caused by outside events like virus scanners.
 *
 * The main purpose is to validate the performance, throughput and stability of the back test engine, not any particular
 * feed, strategy or metric. So the used feed and strategy are optimized for this test and not realistic at all.
 */
private object Performance {

    private const val SKIP = 999 // create signal in 1 out of 999 price-action
    private fun getStrategy(skip: Int): Strategy = FastStrategy(skip)

    /**
     * Try to make the results more reproducible by running the code multiple times and take the best time.
     */
    private inline fun measure(block: () -> Unit): Long {
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
            // sequential runs
            trades = 0
            repeat(backTests) {
                val roboquant = Roboquant(getStrategy(SKIP), logger = SilentLogger())
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
     * Test with three strategies, margin account, shorting, metrics and some logging overhead included
     */
    private fun extendedRun(feed: FastFeed): Long {
        return measure {

            val strategy = CombinedStrategy(
                getStrategy(SKIP - 1),
                getStrategy(SKIP),
                getStrategy(SKIP + 1),
            )

            val broker = SimBroker(accountModel = MarginAccount())
            val policy = FlexPolicy {
                shorting = true
            }

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
                    // use lower channel capacity to limit memory requirements
                    val roboquant = Roboquant(
                        getStrategy(SKIP),
                        logger = SilentLogger(),
                        broker = SimBroker(retention = TimeSpan.ZERO),
                        channelCapacity = 3
                    )
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
            Combination(20_000, 500, 100),
        )

        val header = String.format(
            "\n%8S %6S %6S %4S %7S %7S %10S %7S %6S %9S",
            "candles", "assets", "events", "runs", "feed", "full",
            "sequential", "parallel", "trades", "candles/s"
        )

        println(header)
        println(" " + "━".repeat(header.length - 2))
        for ((events, assets, backTests) in combinations) {
            // single run
            val feed = FastFeed(assets, events)
            val t1 = feedFilter(feed) // Test iterating over feed
            val t2 = extendedRun(feed) // Test a more complete back test

            // multi-run to test engine scalability
            val (t3, trades) = seqRun(feed, backTests)
            val t4 = parRun(feed, backTests)

            // Calculate the total number of candles processed in millions
            val candles = assets * events * backTests / 1_000_000
            val line = String.format(
                "%6dM %7d %6d %4d %5dms %5dms %7dms %7dms %5dK %8dM",
                candles, assets, events, backTests,
                t1, t2, t3, t4,
                (trades / 1_000.0).roundToInt(), feed.size * backTests / (t4 * 1000)
            )
            println(line)
        }
        println()
        exitProcess(0)
    }
}

private object Memory {

    fun test() {
        Config.printInfo()
        val rq = Roboquant(EMAStrategy(), AccountMetric())
        val feed = RandomWalkFeed.lastYears(5, nAssets = 500)
        rq.run(feed)
        exitProcess(0)
    }

}

/**
 * Run the performance test
 */
fun main() {
    if (Config.getProperty("memory") == "true")
        Memory.test()
    else
        Performance.test()
}
