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
import kotlin.test.assertTrue

internal class TimeSeriesChartTest {

    @Test
    fun test() {
        val data = TestData.data

        val chart = TimeSeriesChart(data)

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertTrue(chart.getOption().renderJson().isNotBlank())

        TestData.testFile(chart, "timeserieschart")
    }

    @Test
    fun single() {
        val data = TestData.data
        val chart = TimeSeriesChart(data.values.first())

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertTrue(chart.getOption().renderJson().isNotBlank())
    }

    @Test
    fun predefined() {
        val data = TestData.data
        val chart = TimeSeriesChart.walkForward(data)

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertTrue(chart.getOption().renderJson().isNotBlank())
    }

    @Test
    fun predefined2() {
        val data = TestData.data
        val chart = TimeSeriesChart.walkForward(data)

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertTrue(chart.getOption().renderJson().isNotBlank())
    }

}