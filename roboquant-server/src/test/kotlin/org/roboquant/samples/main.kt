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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.common.Asset
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Timeframe
import org.roboquant.common.hours
import org.roboquant.feeds.Feed
import org.roboquant.feeds.util.LiveTestFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.PriceMetric
import org.roboquant.server.WebServer
import org.roboquant.strategies.EMAStrategy
import kotlin.random.Random
import kotlin.system.exitProcess


private fun getFeed(symbol: String, startPrice: Double): Feed {
    var prev = startPrice
    val randomPrices = (1..10_000).map {
        val next = (prev + Random.Default.nextDouble() - 0.5).coerceAtLeast(10.0)
        prev = next
        next
    }
    return LiveTestFeed(randomPrices, asset = Asset(symbol), delayInMillis = 200)
}

private fun getRoboquant() =
    Roboquant(EMAStrategy(), AccountMetric(), PriceMetric("CLOSE"), logger = MemoryLogger(false))

fun main() {
    val server = WebServer("test", "secret", 8080)

    val jobs = ParallelJobs()

    val tf = Timeframe.next(1.hours)
    jobs.add { server.runAsync(getRoboquant(), getFeed("TSLA", 240.0), tf, "run-tsla") }
    jobs.add { server.runAsync(getRoboquant(), getFeed("AMZN", 140.0), tf, "run-amzn") }
    jobs.add { server.runAsync(getRoboquant(), getFeed("GOOGL", 120.0), tf, "run-googl") }

    jobs.joinAllBlocking()
    server.stop()
    exitProcess(0)
}


