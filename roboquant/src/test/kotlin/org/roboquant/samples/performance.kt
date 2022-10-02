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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.common.ParallelJobs
import org.roboquant.feeds.Feed
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.LastEntryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import kotlin.system.measureTimeMillis

/**
 * Basic test with minimal overhead
 */
fun base() {
    val feed = RandomWalk.lastDays(7, 100)

    repeat(3) {
        val t = measureTimeMillis {
            val roboquant = Roboquant(EMACrossover(), ProgressMetric(), logger = SilentLogger())
            roboquant.run(feed)
        }

        println("time = $t ms")
    }
}



/**
 * Basic test with minimal overhead
 */
fun baseParallel(feed: Feed) = runBlocking {
    val jobs = ParallelJobs()
    var actions = 0

    val t = measureTimeMillis {
        repeat(8) {
            val logger = LastEntryLogger()
            val roboquant = Roboquant(
                EMACrossover(),
                ProgressMetric(),
                logger = logger
            )
            jobs.add {
                roboquant.run(feed)
                actions += logger.getMetric("progress.actions").first().value.toInt()
            }
        }
        jobs.joinAll()
    }
    println("actions = $actions  time = $t")
}

/**
 * Example of hyperparameter search using parallel runs.
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
    val maxEntry = logger.getMetric("account.equity").max()
    println("${maxEntry.info.run}  => ${maxEntry.value} equity")
}

suspend fun main() {
    Config.printInfo()

    // Generate 1.008.000 price bars
    val feed = RandomWalk.lastDays(7, 100)

    var time = Long.MAX_VALUE

    // Repeat three times to exclude Java compile time overhead of first run
    repeat(3) {
        val t = measureTimeMillis {
            when ("BASE") {
                "BASE" -> base()
                "BASE_PARALLEL" -> baseParallel(feed)
                "PARALLEL" -> multiRunParallel(feed)
                "MIXED" -> {
                    base()
                    multiRunParallel(feed)
                }
            }
        }
        if (t < time) time = t
    }

    println("\nFastest time $time ms")

}