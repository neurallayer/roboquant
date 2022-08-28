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

package org.roboquant.logging

import org.roboquant.RunPhase
import org.roboquant.TestData
import org.roboquant.common.Logging
import java.util.logging.Level
import kotlin.test.*

internal class InfoLoggerTest {

    @Test
    fun test() {
        val metrics = TestData.getMetrics()
        Logging.setLevel(Level.INFO)
        val logger = InfoLogger()
        logger.log(metrics, TestData.getRunInfo())
        logger.end(RunPhase.VALIDATE)

        val logger2 = InfoLogger(splitMetrics = true, level = Level.WARNING)
        logger2.log(metrics, TestData.getRunInfo())
        logger2.end(RunPhase.VALIDATE)
    }

}