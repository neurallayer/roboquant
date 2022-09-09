/*
 * Copyright 2020-2022 Neural Layer
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
import org.roboquant.logging.LastEntryLogger
import org.roboquant.strategies.EMACrossover
import kotlin.test.assertTrue

internal class AlphaBetaTest {

    @Test
    fun test() {
        val feed = TestData.feed
        val marketAsset = feed.assets.first()
        val strategy = EMACrossover.EMA_5_15
        val alphaBetaMetric = AlphaBeta(marketAsset, 50)
        val logger = LastEntryLogger()
        val roboquant = Roboquant(strategy, alphaBetaMetric, logger = logger)
        roboquant.run(feed)

        val alpha = logger.getMetric("account.alpha").last().value
        assertTrue(!alpha.isNaN())

        val beta = logger.getMetric("account.beta").last().value
        assertTrue(!beta.isNaN())
    }

}