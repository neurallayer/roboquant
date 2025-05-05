/*
 * Copyright 2020-2025 Neural Layer
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

import kotlinx.coroutines.runBlocking
import org.roboquant.common.*
import org.roboquant.common.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.journals.MultiRunJournal
import org.roboquant.questdb.QuestDBFeed
import org.roboquant.questdb.QuestDBJournal
import org.roboquant.runAsync
import org.roboquant.strategies.EMACrossover
import kotlin.system.measureTimeMillis
import kotlin.test.Ignore
import kotlin.test.Test

internal class QuestDBSamples {

    private val tableName = "pricebars"

    private fun <T> printTimeMillis(key: String, block: () -> T): T {
        val result: T
        val t = measureTimeMillis { result = block() }
        println("$key time=${t}ms")
        return result
    }


    @Test
    @Ignore
    internal fun parallel() = runBlocking {
        val feed = RandomWalk(Timeframe.past(4.years), nAssets = 3)
        val jobs = ParallelJobs()
        val mrj = MultiRunJournal { run -> QuestDBJournal(table = run) }

        for (tf in feed.timeframe.split(1.years)) {
            jobs.add {
                val acc = runAsync(feed, EMACrossover(), journal = mrj.getJournal(), timeframe = tf)
                println(acc)
            }
        }

        jobs.joinAll()
        println("done")
    }



    @Test
    @Ignore
    internal fun read() {

        val f = printTimeMillis("open feed") {
            QuestDBFeed(tableName)
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


}

