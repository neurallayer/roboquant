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

package org.roboquant.loggers

import org.junit.jupiter.api.assertDoesNotThrow
import org.roboquant.TestData
import java.time.Instant
import kotlin.test.Test

internal class ConsoleLoggerTest {

    @Test
    fun consoleLogger() {
        val logger = ConsoleLogger()
        assertDoesNotThrow {
            logger.log(TestData.getMetrics(), Instant.now(), "test")
        }
    }

    @Test
    fun splitMetrics() {
        val logger = ConsoleLogger(splitMetrics = true)
        assertDoesNotThrow {
            logger.log(TestData.getMetrics(), Instant.now(), "test")
        }
    }

}
