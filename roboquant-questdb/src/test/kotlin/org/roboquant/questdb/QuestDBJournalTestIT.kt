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

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.ParallelJobs
import org.roboquant.common.years
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.journals.Journal
import org.roboquant.journals.MultiRunJournal
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMACrossover
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class QuestDBJournalTestIT {

    @TempDir
    lateinit var folder: File

    @TempDir
    lateinit var folder2: File

    private fun simpleRun(journal: Journal) {
        val feed = RandomWalk.lastYears(1)
        org.roboquant.run(feed, EMACrossover(), journal)
    }

    @Test
    fun basic() {
        val logger = QuestDBJournal(AccountMetric(), dbPath = folder.toPath(), table="test-run")
        simpleRun(logger)
        val equity = logger.getMetric("account.equity")
        assertTrue(equity.isNotEmpty())
        val runs = logger.getRuns()
        assertTrue(runs.isNotEmpty())
        logger.removeRun("test-run")
        logger.removeAllRuns()
        logger.close()
    }

    @Test
    fun parallel() = runBlocking{
        val mrj = MultiRunJournal {
            run -> QuestDBJournal(AccountMetric(), dbPath = folder2.toPath(), table=run)
        }
        val feed = RandomWalk.lastYears(10)
        val jobs = ParallelJobs()
        val tfs = feed.timeframe.split(1.years)
        for (tf in tfs) {
            jobs.add {
                val journal = mrj.getJournal()
                org.roboquant.runAsync(feed, EMACrossover(), journal, tf)
            }
        }
        jobs.joinAll()
        assertEquals(mrj.getRuns(), QuestDBJournal.getRuns(folder2.toPath()))
        assertEquals(tfs.size, mrj.getMetric("account.equity").size)
    }

}
