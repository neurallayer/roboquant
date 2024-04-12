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
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.HistoricTestFeed
import org.roboquant.feeds.util.LiveTestFeed
import org.roboquant.journals.BasicJournal
import org.roboquant.journals.MetricsJournal
import org.roboquant.journals.MultiRunJournal
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.PNLMetric
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.TestStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RoboquantTest {

    @Test
    fun simpleRun() {
        val strategy = EMAStrategy()
        val roboquant = Roboquant(strategy)
        assertDoesNotThrow {
            roboquant.run(TestData.feed)
        }
        val summary = roboquant.toString()
        assertTrue(summary.isNotEmpty())

    }


    @Test
    fun testWarmUp() {
        val strategy = TestStrategy()
        val feed = HistoricTestFeed()
        val initial = 101.USD.toWallet()
        val broker = SimBroker(initial)
        val logger = MemoryLogger()
        val roboquant = Roboquant(strategy, broker = broker)
        roboquant.run(feed, Timeframe.INFINITE)
        roboquant.broker.reset()
        assertTrue(logger.history.isEmpty())
        val account = broker.sync()
        assertTrue(account.trades.isEmpty())
        assertEquals(initial, account.cash)
    }

    @Test
    fun runAsync() = runBlocking {
        val strategy = EMAStrategy()

        val roboquant = Roboquant(strategy)
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
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal)
            assertTrue(journal.nEvents > 0)
            assertTrue(journal.nItems > 0)
            assertTrue(journal.nSignals > 0)
            assertTrue(journal.nOrders > 0)
            assertTrue(journal.lastTime != null)
        }
    }


    @Test
    fun walkforward()  {
        val feed = RandomWalkFeed.lastYears(10, 2)
        val tfs = feed.timeframe.split(2.years)
        for (tf in tfs) {
            val strategy = EMAStrategy()
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal, tf)
        }
    }

    @Test
    fun run_with_pb()  {
        assertDoesNotThrow {
            val strategy = EMAStrategy()
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal, progressBar = true)
        }
    }

    @Test
    fun run4()  {
        assertDoesNotThrow {
            val strategy = EMAStrategy()
            val feed = LiveTestFeed(delayInMillis = 30)
            val journal = BasicJournal(false)
            run(feed, strategy, journal, heartbeatTimeout = 10)
            assertTrue(journal.nItems < journal.nEvents)
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
    fun massiveParallel() {
        val feed = TestData.feed
        val jobs = ParallelJobs()

        repeat(50) {
            jobs.add {
                val roboquant = Roboquant(EMAStrategy())
                roboquant.runAsync(feed)
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
                val tf = it
                val acc = runAsync(feed, EMAStrategy(), timeframe = tf)
                println(acc.lastUpdate)
                println(tf)
                assertTrue(acc.lastUpdate in tf)
            }
        }
        jobs.joinAllBlocking()

    }



}
