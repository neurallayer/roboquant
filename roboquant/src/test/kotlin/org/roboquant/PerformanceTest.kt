package org.roboquant

import org.roboquant.common.*
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

internal class PerformanceTest {

    private val check = "TEST_PERFORMANCE"
    private val logger = Logging.getLogger(PerformanceTest::class)

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
            feed.filter<PriceBar> {
                it.asset.symbol == "NOT_A_MATCH"
            }
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


    private fun run(feed: HistoricFeed, size:String) {
        val priceBars = feed.assets.size * feed.timeline.size
        logger.info("size=$size candlesticks=${NumberFormat.getIntegerInstance().format(priceBars)}")
        logger.info("size=$size feed performance=${feedPerformance(feed)}ms")
        logger.info("size=$size base performance=${basePerformance(feed)}ms")
        logger.info("size=$size extended performance=${extendedPerformance(feed)}ms")
        logger.info("size=$size parallel performance=${parallelPerformance(feed)}ms")
    }


    private fun getFeed(events: Int, assets: Int): RandomWalkFeed {
        val timeline = Timeframe.fromYears(1901, 2022).toTimeline(1.days).takeLast(events)
        return RandomWalkFeed(timeline, assets)
    }

    @Test
    fun small() {
        Config.getProperty(check) ?: return

        // 500_000 candle sticks
        val feed = getFeed(5000, 100)
        run(feed, "SMALL")
    }

    @Test
    fun medium() {
        Config.getProperty(check) ?: return

        // 1_000_000 candle sticks
        val feed = getFeed(10_000, 100)
        run(feed, "MEDIUM")
    }

    @Test
    fun large() {
        Config.getProperty(check) ?: return

        // 5_000_000 candle sticks
        val feed = getFeed(10_000, 500)
        run(feed, "LARGE")
    }

    @Test
    fun extraLarge() {
        Config.getProperty(check) ?: return

        // 10_000_000 candle sticks
        val feed = getFeed(10_000, 1_000)
        run(feed, "XLARGE")
    }


}