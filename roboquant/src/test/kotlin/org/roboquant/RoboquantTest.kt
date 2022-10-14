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

package org.roboquant

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.roboquant.brokers.Account
import org.roboquant.common.RoboquantException
import org.roboquant.common.years
import org.roboquant.feeds.Event
import org.roboquant.feeds.test.HistoricTestFeed
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.Metric
import org.roboquant.metrics.MetricResults
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.RandomStrategy
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RoboquantTest {

    @Test
    fun simpleRun() {
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy, AccountMetric(), logger = SilentLogger())
        roboquant.run(TestData.feed)
        val summary = roboquant.summary()
        assertTrue(summary.toString().isNotEmpty())
    }

    @Test
    fun brokenMetric() {
        class MyBrokenMetric : Metric {
            override fun calculate(account: Account, event: Event): MetricResults {
                throw RoboquantException("Broken")
            }

        }

        val feed = HistoricTestFeed(100..101)
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy, MyBrokenMetric(), logger = SilentLogger())
        assertThrows<RoboquantException> {
            roboquant.run(feed)
        }

    }

    @Test
    fun randomStrategy() {
        val strategy = RandomStrategy()
        val logger = MemoryLogger(false)
        val roboquant = Roboquant(strategy, AccountMetric(), logger = logger)

        val feed = TestData.feed
        for (timeframe in feed.split(2.years)) roboquant.run(feed, timeframe)

        assertThrows<IllegalArgumentException> {
            RandomStrategy(1.2)
        }
    }

    @Test
    fun validationPhase() {
        val feed = TestData.feed
        val strategy = EMAStrategy()
        val logger = MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)
        val (train, test) = feed.timeframe.splitTrainTest(0.20)
        roboquant.run(feed, train, test)
        val data = logger.getMetric("progress.events")
        assertEquals(2, data.map { it.info.phase }.distinct().size)
        assertEquals(1, logger.runs.size)
    }

    @Test
    fun runAsync() = runBlocking {
        val strategy = EMAStrategy()

        val roboquant = Roboquant(strategy, AccountMetric(), logger = SilentLogger())
        roboquant.runAsync(TestData.feed)
        assertTrue(roboquant.broker.account.trades.isNotEmpty())
    }

    @Test
    fun reset() {
        val strategy = EMAStrategy()
        val logger = MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, AccountMetric(), logger = logger)
        roboquant.run(TestData.feed)
        assertEquals(1, logger.runs.size)
        val lastHistory1 = logger.history.last()

        roboquant.reset()
        roboquant.run(TestData.feed)
        assertEquals(1, logger.runs.size)
        val lastHistory2 = logger.history.last()

        assertEquals(lastHistory1, lastHistory2)
    }

}