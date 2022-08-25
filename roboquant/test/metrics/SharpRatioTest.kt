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

package org.roboquant.metrics


import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.feeds.test.HistoricTestFeed
import org.roboquant.logging.MemoryLogger
import org.roboquant.strategies.TestStrategy
import kotlin.test.assertTrue

internal class SharpRatioTest {

    @Test
    fun basic() {
        val metric = SharpRatio()
        val (account, event) = TestData.metricInput()
        val result = metric.calculate(account, event)
        assertTrue(result.isEmpty())
    }

    @Test
    fun test2() {
        val feed = HistoricTestFeed(100..200)
        val strategy = TestStrategy()
        val metric = SharpRatio()
        val logger = MemoryLogger(false)
        val roboquant = Roboquant(strategy, metric, logger = logger)
        roboquant.run(feed)

        val sharpRatio = logger.getMetric("portfolio.sharpratio").last().value
        assertTrue(!sharpRatio.isNaN())

        val mean = logger.getMetric("portfolio.mean").last().value
        assertTrue(!mean.isNaN())

        val std = logger.getMetric("portfolio.std").last().value
        assertTrue(!std.isNaN())

    }

}