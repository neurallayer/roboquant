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

package org.roboquant.strategies

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.loggers.MemoryLogger
import org.roboquant.loggers.latestRun
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class EMAStrategyTest {

    @Test
    fun simpleTest() = runBlocking {
        val strategy = EMAStrategy()
        strategy.recording = true
        val roboquant = Roboquant(strategy, logger = MemoryLogger(false))
        roboquant.run(TestData.feed, name = "test")
        val names = roboquant.logger.getMetricNames()

        assertTrue(names.isNotEmpty())
        val metrics = roboquant.logger.getMetric(names.first()).latestRun()
        assertTrue(metrics.isNotEmpty())
    }

    @Test
    fun simple2() {
        val strategy1 = EMAStrategy.PERIODS_5_15
        val strategy2 = EMAStrategy.PERIODS_12_26
        val strategy3 = EMAStrategy.PERIODS_50_200
        assertTrue(!strategy1.recording)
        assertNotEquals(strategy1, strategy2)
        assertNotEquals(strategy2, strategy3)
    }

}
