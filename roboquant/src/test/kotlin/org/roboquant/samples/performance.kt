/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.samples

import org.roboquant.Roboquant
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


/**
 * Try to make the results more reproducible by running the code multiple times and take best timing and
 * always run a GC upfront.
 */
fun measure(block: () -> Unit) : Long {
    var best = Long.MAX_VALUE
    repeat(5) {
        System.gc()
        val t = measureTimeMillis(block)
        if (t < best) best = t
    }
    return best
}



/**
 * Basic test with minimal overhead
 */
fun basePerformance(feed: HistoricFeed): Long {
    return measure {
        val roboquant = Roboquant(EMAStrategy(), logger = SilentLogger())
        roboquant.run(feed)
    }
}


/**
 * Test iterating over the feed
 */
fun feedPerformance(feed: HistoricFeed): Long {
    return measure {
        feed.filter<PriceBar>()
    }
}


/**
 * Test with some metrics and logging overhead included
 */
fun extensivePerformance(feed: HistoricFeed): Long {
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
fun parallelPerformance(feed: HistoricFeed): Long {

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


fun run(feed: HistoricFeed) {
    val priceBars = feed.assets.size * feed.timeline.size
    println("\n***** total number of candlesticks: ${NumberFormat.getIntegerInstance().format(priceBars)} ******")
    println("feed performance: ${feedPerformance(feed)}ms")
    println("base performance: ${basePerformance(feed)}ms")
    println("exte performance: ${extensivePerformance(feed)}ms")
    println("feed performance: ${parallelPerformance(feed)}ms")
}


fun getFeed(events: Int, assets: Int): RandomWalkFeed {
    val timeline = Timeframe.fromYears(1900, 2022).toTimeline(1.days).takeLast(events)
    return RandomWalkFeed(timeline, assets)
}

fun main() {
    Config.printInfo()

    // 500_000 candle sticks
    var feed = getFeed(5000, 100)
    run(feed)

    // 1_000_000 candle sticks
    feed = getFeed(10_000, 100)
    run(feed)

    // 10_000_000 candle sticks
    feed = getFeed(10_000, 1000)
    run(feed)

}