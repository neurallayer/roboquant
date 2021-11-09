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
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.MetricScheduler
import org.roboquant.metrics.OpenPositions
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.RandomStrategy
import java.time.Period
import org.junit.Test
import org.roboquant.metrics.ProgressMetric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RoboquantTest {

    @Test
    fun evaluate() {
        val tf = TimeFrame.fromYears(2005, 2010)
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
    fun twentyYearRandomWalk() {
        val timeline = TimeFrame.fromYears(2000, 2020).toDays(excludeWeekends = true)
        val feed = RandomWalk(timeline, generateBars = false)

        val strategy = EMACrossover()
        val logger = SilentLogger()
        val roboquant = Roboquant(strategy, AccountSummary(), logger = logger)
        roboquant.run(feed)
        assert(logger.events > 0)
    }


    @Test
    fun randomly() {
        val fullTimeFrame = TimeFrame.fromYears(2015, 2020)
        val timeline = fullTimeFrame.toDays(excludeWeekends = true)
        val feed = RandomWalk(timeline)

        val strategy = EMACrossover()

        val reducedMetric = MetricScheduler(MetricScheduler.everyFriday, AccountSummary())
        val roboquant = Roboquant(strategy, reducedMetric, OpenPositions(), logger = SilentLogger())
        roboquant.run(feed)
    }


    @Test
    fun simple_nonblocking() = runBlocking {
        val feed = RandomWalk.lastYears()

        val strategy = EMACrossover()

        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = SilentLogger())
        roboquant.runAsync(feed)
    }

    @Test
    fun simple() {
        val feed = RandomWalk.lastYears()
        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, AccountSummary(), OpenPositions(), logger = SilentLogger())
        roboquant.run(feed)
        val summary = roboquant.summary()
        assertTrue(summary.toString().isNotEmpty())
    }

    @Test
    fun reset() {
        val feed = RandomWalk.lastYears()
        val strategy = EMACrossover()
        val roboquant = Roboquant(strategy, ProgressMetric())
        roboquant.run(feed)
        var runs = roboquant.logger.getRuns()
        assertEquals(1, runs.size)

        roboquant.reset()
        roboquant.run(feed)
        runs = roboquant.logger.getRuns()
        assertEquals(1, runs.size)

    }


}