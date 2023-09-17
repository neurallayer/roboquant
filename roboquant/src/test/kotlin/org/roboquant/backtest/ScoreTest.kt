/*
 * Copyright 2020-2023 Neural Layer
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


import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.Roboquant
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertTrue

class ScoreTest {

    @Test
    fun basic() {
        val feed = RandomWalkFeed.lastYears(1)
        val rq = Roboquant(EMAStrategy.PERIODS_5_15, AccountMetric(), logger = MemoryLogger(false))
        rq.run(feed, name = "run")

        val s1 = MetricScore("account.equity", MetricScore::mean)
        assertTrue(s1.calculate(rq, "run", feed.timeframe).isFinite())

        val sMin = MetricScore("account.equity", MetricScore::min)
        val sMax = MetricScore("account.equity", MetricScore::min)
        assertTrue(sMin.calculate(rq, "run", feed.timeframe) <= sMax.calculate(rq, "run", feed.timeframe))

        assertDoesNotThrow {
            val s = MetricScore("account.equity", MetricScore::annualized)
            s.calculate(rq, "run", feed.timeframe)
        }

        val sWrong = MetricScore("account.equityWrong")
        assertTrue(sWrong.calculate(rq, "run", feed.timeframe).isNaN())

    }

}
