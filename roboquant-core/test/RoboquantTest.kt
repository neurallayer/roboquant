/*
 * Copyright 2021 Neural Layer
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
import org.junit.Test
import org.roboquant.common.Timeframe
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.RandomStrategy
import java.time.Period
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RoboquantTest {

    @Test
    fun evaluate() {
        val tf = Timeframe.fromYears(2008, 2010)
        val timeline = tf.toDays(excludeWeekends = true)
        val feed = RandomWalk(timeline)

        val strategy = RandomStrategy()
        val logger = SilentLogger()
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)

        roboquant.run(feed)
        assertTrue { logger.events > 1 }
    }

    @Test
    fun walkForward() {
        val strategy = RandomStrategy()
        val logger = MemoryLogger(false)
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)

        val feed = RandomWalk.lastYears()
        feed.split(Period.ofYears(2)).forEach {
            roboquant.run(feed, it)
        }
    }

    @Test
    fun tenYearRandomWalk() {
        val timeline = Timeframe.fromYears(2010, 2020).toDays(excludeWeekends = true)
        val feed = RandomWalk(timeline, generateBars = false)

        val strategy = EMACrossover()
        val logger = SilentLogger()
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
        roboquant.run(feed)
        assertTrue(logger.events > 0)
    }


    @Test
    fun validationPhase() {
        val feed = RandomWalk.lastYears()
        val strategy = EMACrossover()
        val logger =  MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)
        val (train, test) = feed.timeframe.splitTrainTest(0.20)
        roboquant.run(feed, train, test)
        val data = logger.getMetric("progress.events")
        assertEquals(2, data.map { it.info.phase }.distinct().size)
        assertEquals(1, logger.runs.size)
    }


    @Test
    fun runAsync() = runBlocking {
        val feed = RandomWalk.lastYears()

        val strategy = EMACrossover()

        val roboquant = Roboquant(strategy, AccountSummary(), logger = SilentLogger())
        roboquant.runAsync(feed)
        assertTrue(roboquant.broker.account.trades.isNotEmpty())
    }

    @Test
    fun simple() {
        val feed = RandomWalk.lastYears(nAssets = 2)
        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, AccountSummary(), logger = SilentLogger())
        roboquant.run(feed)
        val summary = roboquant.summary()
        assertTrue(summary.toString().isNotEmpty())
    }

    @Test
    fun reset() {
        val feed = RandomWalk.lastYears()
        val strategy = EMACrossover()
        val logger = MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
        roboquant.run(feed)
        assertEquals(1, logger.runs.size)
        val lastHistory1 = logger.history.last

        roboquant.reset()
        roboquant.run(feed)
        assertEquals(1, logger.runs.size)
        val lastHistory2 = logger.history.last

        assertEquals(lastHistory1, lastHistory2)
    }


}