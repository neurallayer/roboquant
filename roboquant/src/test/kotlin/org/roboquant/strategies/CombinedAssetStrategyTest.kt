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

package org.roboquant.strategies

import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.TestData
import org.roboquant.logging.SilentLogger
import org.roboquant.metrics.ProgressMetric
import kotlin.test.assertTrue

internal class CombinedAssetStrategyTest {

    @Test
    fun test() {
        val feed = TestData.feed
        val asset1 = feed.assets.first()
        val asset2 = feed.assets.last()
        val strategy = CombinedAssetStrategy {
            when (it) {
                asset1 -> EMAStrategy(1, 3)
                asset2 -> EMAStrategy(3, 5)
                else -> {
                    NoSignalStrategy()
                }
            }
        }

        strategy.enableRecording(false)

        val logger = SilentLogger()
        val roboquant = Roboquant(strategy, ProgressMetric(), logger = logger)

        roboquant.run(feed)
        assertTrue(logger.events > 1)
    }

}