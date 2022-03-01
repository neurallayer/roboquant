@file:Suppress("KotlinConstantConditions")
package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.common.ParallelJobs
import org.roboquant.common.hours
import org.roboquant.feeds.Feed
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.LastEntryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.TAStrategy
import java.time.Instant
import kotlin.system.measureTimeMillis


private fun getFeed(n: Int = 2): Feed {
    val timeline = mutableListOf<Instant>()
    var start = Instant.parse("1975-01-01T09:00:00Z")

    // Create a timeline
    repeat(n * 10_000) {
        timeline.add(start)
        start += 4.hours
    }

    return RandomWalk(timeline, 100)
}


/**
 * Basic test
 */
fun base(feed: Feed) {
    // Create a roboquant using Exponential Weighted Moving Average
    val roboquant = Roboquant(EMACrossover(), ProgressMetric(), policy = DefaultPolicy())
    roboquant.run(feed)
    roboquant.broker.account.summary().print()
}

/**
 * Technical analysis with many indicators
 */
fun technicalAnaslysis(feed: Feed) {
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

    val roboquant = Roboquant(strategy, AccountSummary())
    roboquant.run(feed)
    roboquant.broker.account.summary().print()
}


/**
 * Example of hyper parameter search using parallel runs.
 * Total 36 (6x6) combinations are explored.
 */
suspend fun multiRunParallel(feed: Feed) {
    val logger = LastEntryLogger()
    val jobs = ParallelJobs()

    for (fast in 10..15) {
        for (slow in 20..25) {
            val strategy = EMACrossover(fast, slow)
            val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
            jobs.add {
                roboquant.runAsync(feed, runName = "run $fast-$slow")
            }
        }
    }

    jobs.joinAll() // Make sure we wait for all jobs to finish
    val maxEntry = logger.getMetric("account.equity").maxByOrNull{ it.value }!!
    println("${maxEntry.info.run}  => ${maxEntry.value} equity")
}


suspend fun main() {
    Config.printInfo()
    val feed = getFeed(4)

    val t = measureTimeMillis {
        when ("MIXED") {
            "BASE" -> base(feed)
            "TECHNICAL_ANALYSIS" -> technicalAnaslysis(feed)
            "PARALLEL" -> multiRunParallel(feed)
            "MIXED" -> {
                base(feed)
                technicalAnaslysis(feed)
                multiRunParallel(feed)
            }
        }
    }

    println("\nTime elapsed $t ms")

}