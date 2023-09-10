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

import org.icepear.echarts.charts.pie.PieSeries
import org.icepear.echarts.origin.util.SeriesOption
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class AllocationChartTest {

    @Test
    fun test() {
        val account = TestData.usAccount()
        val chart = AllocationChart(account.positions)
        assertTrue(chart.renderJson().isNotBlank())
    }

    @Test
    fun testPerAssetClass() {
        val account = TestData.usAccount()
        val chart = AllocationChart(account.positions, includeAssetClass = true)
        assertTrue(chart.renderJson().isNotBlank())
    }

    @Test
    fun option() {
        val account = TestData.usAccount()
        val series = AllocationChart(account.positions).getOption().series
        assertTrue(series is Array<*> && series.isArrayOf<SeriesOption>())
        assertTrue(series.first() is PieSeries)
    }

}