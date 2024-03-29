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
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.journals.Journal
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

internal class QuestDBJournalTestIT {

    @TempDir
    lateinit var folder: File


    private fun simpleRun(journal: Journal) {
        val feed = RandomWalkFeed.lastYears(1)
        org.roboquant.run(feed, EMAStrategy(), journal)
    }

    @Test
    fun basic() {
        val logger = QuestDBJournal(AccountMetric(), dbPath = folder.toPath(), table="test-run")
        simpleRun(logger)
        val equity = logger.getMetric("account.equity")
        assertTrue(equity.isNotEmpty())
        logger.close()
    }

}
