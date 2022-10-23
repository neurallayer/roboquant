package org.roboquant

import org.roboquant.common.Config
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.feeds.HistoricFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import java.text.NumberFormat
import kotlin.system.measureTimeMillis
import kotlin.test.Test

internal class Performance {

    /**
     * Try to make the results more reproducible by running the code multiple times and take best timing and
     * always run a GC upfront.
     */
    private fun measure(block: () -> Unit) : Long {
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
    private fun basePerformance(feed: HistoricFeed): Long {
        return measure {
            val roboquant = Roboquant(EMAStrategy(), logger = SilentLogger())
            roboquant.run(feed)
        }
    }


    /**
     * Test iterating over the feed
     */
    private fun feedPerformance(feed: HistoricFeed): Long {
        return measure {
            feed.filter<PriceBar>()
        }
    }


    /**
     * Test with some metrics and logging overhead included
     */
    private fun extendedPerformance(feed: HistoricFeed): Long {
        return measure {
            val roboquant = Roboquant(
                EMAStrategy(),
                AccountMetric(),
                ProgressMetric(),
                logger = MemoryLogger(showProgress = false)
            )
            roboquant.run(feed)
        }
    }



    /**
     * Parallel tests (8) with minimal overhead
     */
    private fun parallelPerformance(feed: HistoricFeed): Long {

        return measure {
            val jobs = ParallelJobs()

            repeat(8) {
                jobs.add {
                    val roboquant = Roboquant(EMAStrategy(it + 10, it + 20), logger = SilentLogger())
                    roboquant.run(feed)
                }
            }
            jobs.joinAllBlocking()
        }
    }


    private fun run(feed: HistoricFeed) {
        val priceBars = feed.assets.size * feed.timeline.size
        println("\n***** total number of candlesticks: ${NumberFormat.getIntegerInstance().format(priceBars)} ******")
        println("feed performance: ${feedPerformance(feed)}ms")
        println("base performance: ${basePerformance(feed)}ms")
        println("extended performance: ${extendedPerformance(feed)}ms")
        println("parallel performance: ${parallelPerformance(feed)}ms")
    }


    private fun getFeed(events: Int, assets: Int): RandomWalkFeed {
        val timeline = Timeframe.fromYears(1900, 2022).toTimeline(1.days).takeLast(events)
        return RandomWalkFeed(timeline, assets)
    }

    @Test
    fun small() {
        Config.getProperty("TEST_PERFORMANCE") ?: return

        // 500_000 candle sticks
        val feed = getFeed(5000, 100)
        run(feed)
    }

    @Test
    fun medium() {
        Config.getProperty("TEST_PERFORMANCE") ?: return

        // 1_000_000 candle sticks
        val feed = getFeed(10_000, 100)
        run(feed)
    }

    @Test
    fun large() {
        Config.getProperty("TEST_PERFORMANCE") ?: return

        // 5_000_000 candle sticks
        val feed = getFeed(10_000, 500)
        run(feed)
    }

    @Test
    fun extraLarge() {
        Config.getProperty("TEST_PERFORMANCE") ?: return

        // 10_000_000 candle sticks
        val feed = getFeed(10_000, 1_000)
        run(feed)
    }


}