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

package org.roboquant.strategies

import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.logging.MemoryLogger
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.ProgressMetric
import org.junit.Test
import kotlin.test.assertTrue

internal class TAStrategyTest {

    @Test
    fun test() {

        val strategy = TAStrategy(50)
        strategy.buy { price ->
            ta.ema(price.close, 30) > ta.ema(price.close, 50) && ta.cdlMorningStar(price)
        }

        strategy.sell { price ->
            ta.cdl3BlackCrows(price) || (ta.cdl2Crows(price, 1)  && ta.ema(price.close, 30) < ta.ema(price.close, 50))
        }

        val logger = SilentLogger()
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)

        val feed = RandomWalk.lastYears()
        roboquant.run(feed)
        assertTrue(logger.events > 0)
    }

    private fun runStrategy(s : TAStrategy): SilentLogger {
        val logger = SilentLogger()
        val roboquant = Roboquant(s, ProgressMetric(), logger = logger)
        val feed = RandomWalk.lastYears()
        roboquant.run(feed)
        return logger
    }


    private fun runStrategy2(s : TAStrategy): Roboquant<MemoryLogger> {
        val roboquant = Roboquant(s, ProgressMetric(), logger = MemoryLogger(showProgress = false))
        val feed = RandomWalk.lastYears()
        roboquant.run(feed)
        return roboquant
    }

    @Test
    fun testPredefined() {
        var strategy = TAStrategy.emaCrossover(50, 10)
        var logger = runStrategy(strategy)
        assertTrue(logger.events > 0)

        strategy = TAStrategy.smaCrossover(50, 10)
        logger = runStrategy(strategy)
        assertTrue(logger.events > 0)

        strategy = TAStrategy.recordHighLow(100)
        logger = runStrategy(strategy)
        assertTrue(logger.events > 0)
    }


    @Test
    fun test2() {
        val strategy = TAStrategy(2)
        strategy.buy {
            true
        }

        val exp = runStrategy2(strategy)
        exp.logger.summary().log()
        assertTrue(exp.broker.account.orders.size > 0)
    }


    @Test
    fun testMetrics() {
        val strategy = TAStrategy(10)
        strategy.buy {
            val a = ta.sma(it.close, 5)
            val b = ta.sma(it.close, 10)
            record("sma.fast", a)
            record("sma.slow", b)
            a > b
        }

        val exp = runStrategy2(strategy)
        exp.logger.summary().log()
        assertTrue(exp.logger.getMetricNames().contains("ta.sma.fast"))
        assertTrue(exp.logger.getMetricNames().contains("ta.sma.slow"))

    }
}