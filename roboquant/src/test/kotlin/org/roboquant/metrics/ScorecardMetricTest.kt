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

package org.roboquant.metrics

import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.common.Timeframe
import org.roboquant.loggers.SilentLogger
import org.roboquant.backtest.Score
import org.roboquant.strategies.EMAStrategy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


internal class ScorecardMetricTest {

    @Test
    fun test() {
        val metric = ScorecardMetric()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isNotEmpty())
        assertEquals(0.0, result["scorecard.winners"])
        assertEquals(0.0, result["scorecard.losers"])
        assertNull(result["otherprefix.losers"])

        metric.reset()
        val result2 = metric.calculate(account, event)
        assertEquals(result, result2)
    }

    @Test
    fun annual() {
        val rq = Roboquant(EMAStrategy(), logger = SilentLogger())
        rq.run(TestData.feed)

        val score = Score.annualizedEquityGrowth(rq, Timeframe.fromYears(2019, 2020))
        assertTrue(score.isFinite())
    }

}