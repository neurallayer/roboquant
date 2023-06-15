/*
 * Copyright 2020-2023 Neural Layer
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
import org.roboquant.common.ParallelJobs
import org.roboquant.common.RoboquantException
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.loggers.latestRun
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.Metric
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
        val summary = roboquant.toString()
        assertTrue(summary.isNotEmpty())
        assertEquals(
            "strategy=EMAStrategy policy=FlexPolicy logger=SilentLogger metrics=[AccountMetric] broker=SimBroker",
            summary
        )

        val account = roboquant.broker.account
        assertTrue(account.trades.isNotEmpty())
        assertTrue(account.positions.isEmpty())
        assertTrue(account.openOrders.isEmpty())
        assertTrue(account.closedOrders.isNotEmpty())
    }

    @Test
    fun liquidateTest() {
        val broker = SimBroker()
        val event = TestData.event()
        var account = broker.place(listOf(TestData.usMarketOrder()), event)
        assertEquals(1, account.positions.size)
        assertEquals(1, account.trades.size)
        assertEquals(1, account.closedOrders.size)

        val logger = MemoryLogger(showProgress = false)
        logger.start("test", Timeframe.INFINITE)
        val roboquant = Roboquant(EMAStrategy(), AccountMetric(), broker = broker, logger = logger)
        var equity = roboquant.logger.getMetric("account.equity")
        assertEquals(0, equity.size)
        roboquant.closePositions(runName = "test")
        account = broker.account
        assertEquals(0, account.openOrders.size)
        assertEquals(0, account.positions.size)
        assertEquals(2, account.trades.size)
        assertEquals(2, account.closedOrders.size)

        equity = roboquant.logger.getMetric("account.equity")
        assertEquals(1, equity.size)
    }

    @Test
    fun testDefaultTimeframe() {
        val strategy = TestStrategy()
        val feed = HistoricTestFeed()
        val timeline = feed.timeline
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = LastEntryLogger())
        roboquant.run(feed, name = "test")
        val entry = roboquant.logger.getMetric("progress.steps").latestRun().last()
        assertEquals(timeline.size, entry.value.toInt())

        val offset = 3
        val timeframe = Timeframe(timeline[2], timeline[2 + offset], inclusive = false)
        roboquant.reset()
        roboquant.run(feed, timeframe, name = "test")
        val step2 = roboquant.logger.getMetric("progress.steps").latestRun().last()
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
        val step = roboquant.logger.getMetric("progress.steps").values.last().last()
        assertEquals(3, step.value.toInt())
        assertEquals(timeline[4], step.time)
    }

    @Test
    fun brokenMetric() {
        class MyBrokenMetric : Metric {
            override fun calculate(account: Account, event: Event): Map<String, Double> {
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

        roboquant.reset()
        assertTrue(logger.history.isEmpty())
    }

    @Test
    fun massiveParallel() {
        val feed = TestData.feed
        val jobs = ParallelJobs()

        repeat(50) {
            jobs.add {
                val roboquant = Roboquant(EMAStrategy(), logger = SilentLogger())
                roboquant.runAsync(feed)
            }
        }
        jobs.joinAllBlocking()

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