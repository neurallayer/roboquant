package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.USD
import org.roboquant.common.days
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.LastEntryLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.strategies.EMACrossover
import java.time.Instant


private fun millionBars2(n:Int = 2) {
    val timeline = mutableListOf<Instant>()
    var start = Instant.parse("1965-01-01T09:00:00Z")

    // Create a timeline of n million entries
    repeat(n * 10_000) {
        timeline.add(start)
        start += 1.days
    }

    // Create a random walk for the timeline provided, totol 2_000_000 candle sticks
    val feed = RandomWalk(timeline, 100)

    val logger = LastEntryLogger()
    for (i in 1..10) {
        for (j in i+1..i+10) {
            val strategy = EMACrossover(i, j)
            val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
            val run = "run-$i-$j"
            roboquant.run(feed, runName = run)
            val result = logger.getMetric("account.equity").last { it.info.run == run }.value
            println("$run => ${result.USD}")
        }
    }


}

fun main() {
    millionBars2(2)
}
