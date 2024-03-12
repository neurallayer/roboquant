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

package org.roboquant.questdb

import org.junit.jupiter.api.io.TempDir
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class QuestDBMetricsLoggerTestIT {

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
        val logger = QuestDBMetricsLogger(folder.toPath())
        logger.removeRun("myrun")
        val feed = RandomWalkFeed.lastYears(1)
        val rq = Roboquant(EMAStrategy(), AccountMetric(), logger = logger)
        rq.run(feed, name = "myrun")
        val equity = logger.getMetric("account.equity", "myrun")
        assertTrue(equity.isNotEmpty())

        logger.log(mapOf("aaa" to 12.0), Instant.now(), "myrun")
        val equity2 = logger.getMetric("account.equity", "myrun")
        assertTrue(equity2.isNotEmpty())

        val aaa = logger.getMetric("aaa", "myrun")
        assertEquals(1, aaa.size)

        feed.close()

        logger.close()

        val logger2 = QuestDBMetricsLogger(folder.toPath())
        logger2.loadPreviousRuns()
        val aaa2 = logger2.getMetric("aaa", "myrun")
        assertEquals(1, aaa2.size)
        logger2.close()

        val logger3 = QuestDBMetricsLogger(folder.toPath())
        logger3.removeAllRuns()
        logger3.loadPreviousRuns()
        val aaa3 = logger3.getMetric("aaa", "myrun")
        assertEquals(0, aaa3.size)
        logger3.close()
    }

}
