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
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Timeframe
import org.roboquant.common.months
import org.roboquant.common.seconds
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.questdb.QuestDBFeed
import org.roboquant.questdb.QuestDBMetricsLogger
import org.roboquant.questdb.QuestDBRecorder
import org.roboquant.strategies.EMAStrategy
import kotlin.system.measureTimeMillis


private const val TABLE_NAME = "pricebars"

private fun <T>printTimeMillis(key: String, block: () -> T) : T {
    val result: T
    val t = measureTimeMillis { result = block() }
    println("$key time=${t}ms")
    return result
}

private fun create() {
    val f = RandomWalkFeed(Timeframe.past(12.months), nAssets = 3, timeSpan = 1.seconds)

    printTimeMillis("create feed") {
        val g = QuestDBRecorder()
        g.removeAllFeeds()
        g.record<PriceBar>(f, TABLE_NAME )
    }
}


private fun read() {

    val f = printTimeMillis("open feed") {
        QuestDBFeed(TABLE_NAME)
    }

    printTimeMillis("get assets") {
        println(f.assets.joinToString("\n"))
    }

    printTimeMillis("get timeframe") {
        println(f.timeframe)
    }

    printTimeMillis("iterate 3 months") {
        f.filter<PriceBar>(Timeframe.past(3.months)) {
            false
        }
    }

    f.close()
}


private fun backTest() {
    val feed = QuestDBFeed(TABLE_NAME)
    val jobs = ParallelJobs()
    val logger = QuestDBMetricsLogger()
    logger.removeAllRuns()
    feed.timeframe.split(1.months).forEach { tf ->
        jobs.add {
            val run = "run-${tf.toPrettyString()}"
            println("starting run=$run")
            val broker = SimBroker(limitTracking = true) // Set to true to optimize for performance
            val rq = Roboquant(EMAStrategy(), AccountMetric(), broker = broker, logger = logger)
            rq.runAsync(feed, tf, name = run)
            println("done run=$run")
        }
    }

    jobs.joinAllBlocking()

    feed.close()
    logger.close()
}


fun main() {
    create()
    read()
    backTest()
}