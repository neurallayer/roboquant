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
import org.roboquant.common.ParallelJobs
import org.roboquant.common.months
import org.roboquant.common.years
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.journals.BasicJournal
import org.roboquant.journals.MemoryJournal
import org.roboquant.journals.MultiRunJournal
import org.roboquant.metrics.PNLMetric
import org.roboquant.strategies.EMACrossover
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RunTest {

    @Test
    fun simpleRun() {
        assertDoesNotThrow {
            run(TestData.feed, EMACrossover())
        }
    }



    @Test
    fun runAsyncTest() = runBlocking {

        assertDoesNotThrow {
            runBlocking {
                runAsync(TestData.feed, EMACrossover())
            }
        }
    }


    @Test
    fun run2()  {
        assertDoesNotThrow {
            val strategy = EMACrossover()
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal = journal)
            assertTrue(journal.nEvents > 0)
            assertTrue(journal.nItems > 0)
            assertTrue(journal.nOrders > 0)
            assertTrue(journal.lastTime != null)
            assertTrue(journal.maxPositions > 0)
        }
    }


    @Test
    fun walkforward()  {
        val feed = RandomWalk.lastYears(10, 2)
        val tfs = feed.timeframe.split(2.years)
        for (tf in tfs) {
            val strategy = EMACrossover()
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal=journal, timeframe = tf)
        }
    }

    @Test
    fun run_with_pb()  {
        assertDoesNotThrow {
            val strategy = EMACrossover()
            val journal = BasicJournal()
            run(TestData.feed, strategy, journal = journal, showProgressBar = true)
        }
    }



    @Test
    fun run3()  {
        val mrj = MultiRunJournal { MemoryJournal(PNLMetric()) }
        val feed = TestData.feed
        val timeframes  = feed.timeframe.split(1.years)
        for (tf in timeframes) {
            val strategy = EMACrossover()
            val run = tf.toString()
            run(TestData.feed, strategy, journal = mrj.getJournal(run), timeframe = tf)
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
                runAsync(feed, EMACrossover())
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
                val acc = runAsync(feed, EMACrossover(), timeframe = tf)
                println(acc.lastUpdate)
                println(tf)
                assertTrue(acc.lastUpdate in tf)
            }
        }
        jobs.joinAllBlocking()

    }



}
