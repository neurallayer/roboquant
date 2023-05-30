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

package org.roboquant.jupyter

import org.icepear.echarts.charts.heatmap.HeatmapSeries
import org.icepear.echarts.origin.util.SeriesOption
import org.junit.jupiter.api.Test
import org.roboquant.feeds.RandomWalkFeed
import kotlin.test.assertTrue

internal class CorrelationChartTest {

    @Test
    fun test() {
        val feed = RandomWalkFeed.lastYears(1, 5)
        val chart = CorrelationChart(feed, feed.assets)
        assertTrue(chart.asHTML().isNotBlank())
    }

    @Test
    fun option() {
        val feed = RandomWalkFeed.lastYears(1, 5)
        val series = CorrelationChart(feed, feed.assets).getOption().series
        assertTrue(series is Array<*> && series.isArrayOf<SeriesOption>())
        assertTrue(series.first() is HeatmapSeries)
    }

}