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

package org.roboquant

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.roboquant.brokers.Account
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.SilentLogger
import org.roboquant.loggers.latestRun
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.Metric
import org.roboquant.metrics.PNLMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.TestStrategy
import kotlin.test.*

internal class RoboquantTest {

    @Test
    fun simpleRun() {
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy, AccountMetric(), logger = SilentLogger())
        assertDoesNotThrow {
            roboquant.run(TestData.feed)
        }
        val summary = roboquant.toString()
        assertTrue(summary.isNotEmpty())
        assertEquals(
            "strategy=EMAStrategy policy=FlexPolicy logger=SilentLogger metrics=[AccountMetric] broker=SimBroker",
            summary
        )

    }

    @Test
    fun liquidateTest() {
        val broker = SimBroker()
        val event = TestData.event()
        broker.place(listOf(TestData.usMarketOrder()))
        var account = broker.sync(event)
        assertEquals(1, account.positions.size)
        assertEquals(1, account.trades.size)
        assertEquals(1, account.closedOrders.size)

        val logger = MemoryLogger(showProgress = false)
        logger.start("test", Timeframe.INFINITE)
        val roboquant = Roboquant(EMAStrategy(), AccountMetric(), broker = broker, logger = logger)
        var equity = roboquant.logger.getMetric("account.equity")
        assertEquals(0, equity.size)
        account = roboquant.closePositions(runName = "test")
        assertEquals(0, account.openOrders.size)
        assertEquals(0, account.positions.size)
        assertEquals(1, account.trades.size)
        assertEquals(1, account.closedOrders.size)

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
        val entry = roboquant.logger.getMetric("progress.events").latestRun().last()
        assertEquals(timeline.size, entry.value.toInt())

        val offset = 3
        val timeframe = Timeframe(timeline[2], timeline[2 + offset], inclusive = false)
        roboquant.reset()
        roboquant.run(feed, timeframe, name = "test")
        val step2 = roboquant.logger.getMetric("progress.events").latestRun().last()
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
        val step = roboquant.logger.getMetric("progress.events").values.last().last()
        assertEquals(3, step.value.toInt())
        assertEquals(timeline[4], step.time)
    }

    @Test
    fun testWarmUp() {
        val strategy = TestStrategy()
        val feed = HistoricTestFeed()
        val initial = 101.USD.toWallet()
        val broker = SimBroker(initial)
        val logger = MemoryLogger()
        val roboquant = Roboquant(strategy, ProgressMetric(), broker = broker, logger = logger)
        roboquant.copy(logger = SilentLogger()).run(feed, Timeframe.INFINITE)
        roboquant.broker.reset()
        assertTrue(logger.history.isEmpty())
        val account = broker.sync()
        assertTrue(account.trades.isEmpty())
        assertEquals(initial, account.cash)
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
        assertDoesNotThrow {
            runBlocking {
                roboquant.runAsync(TestData.feed)
            }
        }
    }


    @Test
    fun run2()  {
        assertDoesNotThrow {
            val strategy = EMAStrategy()
            run(TestData.feed, strategy, journal = BasicJournal())
        }
    }

    @Test
    fun run3()  {
        val mrj = MultiRunJournal { MetricsJournal(PNLMetric()) }
        val feed = TestData.feed
        val timeframes  = feed.timeframe.split(1.years)
        for (tf in timeframes) {
            val strategy = EMAStrategy()
            val run = tf.toString()
            run(TestData.feed, strategy, journal = mrj.getJournal(run), tf)
        }
        val m = mrj.getMetric("pnl.equity")
        assertEquals(timeframes.size, m.size)
    }

    @Test
    fun reset() {
        val strategy = EMAStrategy()
        val logger = MemoryLogger(showProgress = false)
        val roboquant = Roboquant(strategy, AccountMetric(), logger = logger)
        roboquant.run(TestData.feed)
        assertEquals(1, logger.getRuns().size)

        roboquant.reset()
        assertTrue(logger.history.isEmpty())
    }

    @Test
    fun massiveParallel() {
        val feed = TestData.feed
        val jobs = ParallelJobs()

        repeat(50) {
            jobs.add {
                val roboquant = Roboquant(EMAStrategy(), ProgressMetric(), logger = LastEntryLogger())
                val run = "run-$it"
                roboquant.runAsync(feed, name = run)
                val metric = roboquant.logger.getMetric("progress.events", run)
                assertEquals(1, metric.size)
                assertEquals(feed.timeline.size.toDouble(), metric.values[0])
            }
        }
        jobs.joinAllBlocking()

    }


    @Test
    fun parallelTimeframes() {
        val feed = TestData.feed
        val jobs = ParallelJobs()

        feed.timeframe.sample(3.months).forEach {
            jobs.add {
                val roboquant = Roboquant(EMAStrategy(), ProgressMetric(), logger = MemoryLogger(false))
                val run = "run-$it"
                roboquant.runAsync(feed, it, name = run)
                val metric = roboquant.logger.getMetric("progress.events", run)
                val tf = metric.timeframe
                assertFalse(tf.isEmpty())
                assertTrue(tf.start in it)
                assertTrue(tf.end in it)
            }
        }
        jobs.joinAllBlocking()

    }



}
