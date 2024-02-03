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
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OptimizerTest {

    @Test
    fun basic() {
        val space = GridSearch()
        space.add("x", 3..15)
        space.add("y", 2..10)

        val opt = Optimizer(space, "account.equity") { params ->
            val x = params.getInt("x")
            val y = x + params.getInt("y")
            val s = EMAStrategy(x, y)
            Roboquant(s, AccountMetric(), logger = LastEntryLogger())
        }

        val feed = RandomWalkFeed.lastYears(1, nAssets = 1)

        val r1 = opt.train(feed, feed.timeframe).maxBy { it.score }
        assertTrue(r1.score.isFinite())

        val r2 = opt.walkForward(feed, 6.months, 2.months)
        assertTrue(r2.isNotEmpty())

        val r3 = opt.monteCarlo(feed, 6.months, 3.months, 5)
        assertTrue(r3.isNotEmpty())

        assertTrue(r3.min().containsKey("training"))
        assertTrue(r3.max().containsKey("training"))
        assertTrue(r3.average().containsKey("training"))
        assertTrue(r3.correlation().isFinite())
        assertEquals(r3.size, r3.clean().size)
    }

    @Test
    fun complete() {
        val space = GridSearch()
        space.add("x", 3..15)
        space.add("y", 2..10)

        val logger = LastEntryLogger()
        val opt = Optimizer(space, "account.equity") { params ->
            val x = params.getInt("x")
            val y = x + params.getInt("y")
            val s = EMAStrategy(x, y)
            Roboquant(s, AccountMetric(), logger = logger)
        }

        val feed = RandomWalkFeed.lastYears(3, nAssets = 2)
        val r2 = opt.walkForward(feed, 9.months, 3.months, 0.months, false)
        assertTrue(r2.isNotEmpty())
    }

    @Test
    fun noParams() {
        val space = EmptySearchSpace()
        val opt = Optimizer(space, "account.equity") {
            Roboquant(EMAStrategy(), AccountMetric(), logger = LastEntryLogger())
        }

        val feed = RandomWalkFeed.lastYears(1, nAssets = 1)
        val r1 = opt.train(feed, feed.timeframe).maxBy { it.score }
        assertTrue(r1.score.isFinite())

    }


}
