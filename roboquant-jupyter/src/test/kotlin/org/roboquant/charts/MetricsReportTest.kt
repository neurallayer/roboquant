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

package org.roboquant.charts


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.Roboquant
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import java.io.File
import kotlin.test.assertTrue

internal class MetricsReportTest {

    @TempDir
    lateinit var folder: File

    @Test
    fun test() {
        val f = RandomWalkFeed.lastYears(1, 1, generateBars = true)
        val rq = Roboquant(EMAStrategy(), AccountMetric())
        assertDoesNotThrow {
            val report = MetricsReport(rq)
            val file = File(folder, "test.html")
            report.toHTMLFile(file.toString())
            assertTrue(file.exists())
        }

        rq.run(f)
        assertDoesNotThrow {
            val report = MetricsReport(rq)
            val file = File(folder, "test.html")
            report.toHTMLFile(file.toString())
            assertTrue(file.exists())
        }
    }


}