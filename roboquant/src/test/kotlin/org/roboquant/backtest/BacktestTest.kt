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

package org.roboquant.backtest

import org.roboquant.Roboquant
import org.roboquant.common.months
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BacktestTest {

    private val feed = RandomWalkFeed.lastYears(2)

    @Test
    fun single() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.singleRun()
        val data = rq.logger.getMetric("account.equity")
        assertEquals(1, data.size)
    }

    @Test
    fun wf() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.walkForward(6.months)
        val data = rq.logger.getMetric("account.equity")
        assertTrue(data.size >= 3)
    }

    @Test
    fun mc() {
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        val bt = Backtest(feed, rq)
        bt.monteCarlo(3.months, 50)
        val data = rq.logger.getMetric("account.equity")
        assertEquals(50, data.size)
    }


}
