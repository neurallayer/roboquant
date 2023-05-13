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

package org.roboquant.loggers

import org.roboquant.TestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SilentLoggerTest {

    @Test
    fun silentLogger() {
        val logger = SilentLogger()
        logger.log(TestData.getMetrics(), TestData.getStep())
        assertEquals(1, logger.events)
        assertTrue(logger.metricNames.isEmpty())
        assertTrue(logger.getMetric("key1").isEmpty())

        logger.log(TestData.getMetrics(), TestData.getStep())
        assertEquals(2, logger.events)

        logger.reset()
        assertEquals(0L, logger.events)
    }
}