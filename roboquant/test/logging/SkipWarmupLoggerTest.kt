/*
 * Copyright 2022 Neural Layer
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

package org.roboquant.logging

import org.roboquant.RunInfo
import org.roboquant.RunPhase
import org.roboquant.TestData
import org.roboquant.common.Timeframe
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SkipWarmupLoggerTest {

    @Test
    fun test() {
        val metrics = TestData.getMetrics()
        val logger = MemoryLogger(showProgress = false).skipFirst(10)

        repeat(9) {
            val info = RunInfo("run-1", 1, it, Instant.now(), Timeframe.INFINITE, RunPhase.MAIN)
            logger.log(metrics, info)
        }
        assertTrue(logger.metricNames.isEmpty())

        repeat(4) {
            val info = RunInfo("run-1", 1, it + 9, Instant.now(), Timeframe.INFINITE, RunPhase.MAIN)
            logger.log(metrics, info)
        }
        assertFalse(logger.metricNames.isEmpty())
    }

}