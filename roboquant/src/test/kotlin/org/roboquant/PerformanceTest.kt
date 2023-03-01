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

package org.roboquant

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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
import org.roboquant.strategies.EMAStrategy
import kotlin.system.measureTimeMillis


private class WrappingFeed(feed: Feed) : Feed {

    private val events: List<Event>

    init {
        events = feed.toList()
    }


    override suspend fun play(channel: EventChannel) {
        for (event in events) {
            channel.send(event)
        }
    }

}

/**
 * Performance test that runs a number of back-tests scenarios against different feed sizes to measure performance
 * and detect possible performance regressions. Each test is run 3 times in order to minimize fluctuations cause by
 * outside events like virus scanners.
 *
 * The main purpose is to validate the performance, throughput and stability of the back test engine, not any particular
 * feed, strategy or metric.
 */
internal class PerformanceTest {

    private val concurrency = "parallel"
    private val logger = Logging.getLogger(PerformanceTest::class)
    private val parallel = Config.getProperty(concurrency)?.toInt() ?: Config.info.cores

    /**
     * Try to make the results more reproducible by running the code multiple times and take best timing.
     */
    private fun measure(block: () -> Unit): Long {
        var best = Long.MAX_VALUE
        repeat(3) {
            val t = measureTimeMillis(block)
            if (t < best) best = t
        }
        return best
    }


    /**
     * Basic test with minimal overhead
     */
    private fun baseRun(feed: Feed): Long {
        return measure {
            val roboquant = Roboquant(EMAStrategy(), logger = SilentLogger())
            roboquant.run(feed)
        }
    }


    /**
     * Test iterating over the feed while filtering
     */
    private fun feedFilter(feed: Feed): Long {
        return measure {
            feed.filter<PriceBar> {
                it.asset.symbol == "NOT_A_MATCH"
            }
        }
    }


    /**
     * Test with 3 strategies, margin account, shorting, extra metrics and logging overhead included
     */
    private fun extendedRun(feed: Feed): Long {
        return measure {

            val strategy = CombinedStrategy(
                EMAStrategy.PERIODS_5_15,
                EMAStrategy.PERIODS_12_26,
                EMAStrategy.PERIODS_50_200
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
     * Parallel tests
     */
    private fun parallelRuns(feed: Feed): Long {

        return measure {
            val jobs = ParallelJobs()
            repeat(parallel) {
                jobs.add {
                    val roboquant = Roboquant(EMAStrategy(), logger = SilentLogger())
                    roboquant.runAsync(feed)
                }
            }
            jobs.joinAllBlocking()
        }
    }

    private fun log(name: String, t: Long) =
        logger.info {
            "    %-20s%8d ms".format(name, t)
        }

    private fun run(origFeed: HistoricFeed) {
        val priceBars = origFeed.assets.size * origFeed.timeline.size
        val size = "%,10d".format(priceBars)
        logger.info("***** $size candlesticks *****")

        // We wrap the feed, so we don't measure the performance of
        // the random generator of the RandomWalkFeed.
        val feed = WrappingFeed(origFeed)
        log("feed filter", feedFilter(feed))
        log("base run", baseRun(feed))
        val p = parallelRuns(feed)
        log("parallel runs (x$parallel)", p)
        log("extended run", extendedRun(feed))
        val throughput = priceBars * parallel.toLong() / (p * 1000L)
        logger.info("    throughput $throughput million candles/s")
    }

    private fun getFeed(events: Int, assets: Int): RandomWalkFeed {
        val timeline = Timeframe.fromYears(1901, 2022).toTimeline(1.days).takeLast(events)
        return RandomWalkFeed(timeline, assets)
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun info() {
            Config.printInfo()
        }
    }

    @Test
    fun size1() {
        Config.getProperty("FULL_COVERAGE") != null && return
        // 500_000 candle sticks
        val feed = getFeed(5000, 100)
        run(feed)
    }

    @Test
    fun size2() {
        Config.getProperty("FULL_COVERAGE") !=  null && return
        // 1_000_000 candle sticks
        val feed = getFeed(10_000, 100)
        run(feed)
    }

    @Test
    fun size3() {
        Config.getProperty("FULL_COVERAGE") !=  null && return
        // 5_000_000 candle sticks
        val feed = getFeed(10_000, 500)
        run(feed)
    }

    @Test
    fun size4() {
        Config.getProperty("FULL_COVERAGE") !=  null && return
        // 10_000_000 candle sticks
        val feed = getFeed(10_000, 1_000)
        run(feed)
    }




}
