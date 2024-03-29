/*
 * Copyright 2020-2024 Neural Layer
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

import org.roboquant.common.Timeframe
import org.roboquant.common.months
import org.roboquant.common.seconds
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.questdb.QuestDBFeed
import org.roboquant.questdb.QuestDBRecorder
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
    internal fun create() {
        val f = RandomWalkFeed(Timeframe.past(12.months), nAssets = 3, timeSpan = 1.seconds)

        printTimeMillis("create feed") {
            val g = QuestDBRecorder()
            g.removeAllFeeds()
            g.record<PriceBar>(f, tableName)
        }
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

