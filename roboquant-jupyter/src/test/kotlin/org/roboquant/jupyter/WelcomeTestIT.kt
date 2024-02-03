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

package org.roboquant.jupyter

import org.roboquant.charts.PriceBarChart
import org.roboquant.charts.TimeSeriesChart
import kotlin.test.Test
import kotlin.test.assertTrue

internal class WelcomeTestIT {

    @Test
    fun test() {
        val w = Welcome()
        val snippet = w.asHTML()
        assertTrue(snippet.isNotBlank())
        assertTrue { w.asHTMLPage().contains(snippet) }
    }

    @Test
    fun testDemo1() {
        val chart1 = Welcome().demo1()
        assertTrue(chart1 is TimeSeriesChart)
    }

    @Test
    fun testDemo2() {
        val chart2 = Welcome().demo2()
        assertTrue(chart2 is TimeSeriesChart)
    }

    @Test
    fun testDemo3() {
        val chart3 = Welcome().demo3()
        assertTrue(chart3 is PriceBarChart)
    }

}
