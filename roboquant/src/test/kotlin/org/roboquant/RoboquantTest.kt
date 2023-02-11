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
import org.roboquant.brokers.sim.NoCostPricingEngine
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.RoboquantException
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.Metric
import org.roboquant.metrics.MetricResults
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.TestStrategy
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    fun testDefaultTimeframe() {
        val strategy = TestStrategy()
        val feed = HistoricTestFeed()
        val timeline = feed.timeline
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = LastEntryLogger())
        roboquant.run(feed)
        val step = roboquant.logger.getMetric("progress.steps").last()
        assertEquals(timeline.size, step.value.toInt())
        assertEquals(RunPhase.MAIN, step.info.phase)
        assertEquals(timeline.size, step.info.step)

        val offset = 3
        val timeframe = Timeframe(timeline[2], timeline[2 + offset], inclusive = false)
        roboquant.reset()
        roboquant.run(feed, timeframe)
        val step2 = roboquant.logger.getMetric("progress.steps").last()
        assertEquals(offset, step2.value.toInt())
    }

    @Test
    fun testTimeframe() {
        val strategy = TestStrategy()
        val feed = HistoricTestFeed()
        val timeline = feed.timeline
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = LastEntryLogger())

        val timeframe = Timeframe(timeline[2], timeline[5], inclusive = false)
        roboquant.run(feed, timeframe)
        val step = roboquant.logger.getMetric("progress.steps").last()
        assertEquals(3, step.value.toInt())
        assertEquals(timeline[4], step.info.time)
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
    fun validationPhase() {
        val feed = TestData.feed
        val strategy = EMAStrategy()
        val logger = MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)
        val (train, test) = feed.timeframe.splitTrainTest(0.20)
        roboquant.run(feed, timeframe = train, validation = test)
        val data = logger.getMetric("progress.steps")
        assertEquals(2, data.map { it.info.phase }.distinct().size)
        assertEquals(1, data.map { it.info.run }.distinct().size)
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

    @Test
    fun prices() {
        val feed = TestData.feed
        val rq =
            Roboquant(EMAStrategy(), broker = SimBroker(pricingEngine = NoCostPricingEngine()), logger = SilentLogger())
        rq.run(feed)

        val trades = rq.broker.account.trades
        assertTrue(trades.isNotEmpty())
        for (trade in trades) {
            val tf = Timeframe(trade.time, trade.time, true)
            val pricebar = feed.filter<PriceAction>(timeframe = tf).firstOrNull { it.second.asset == trade.asset }
            assertNotNull(pricebar)
            assertEquals(pricebar.second.getPrice(), trade.price)
        }
    }

}