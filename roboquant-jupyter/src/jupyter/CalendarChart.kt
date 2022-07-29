/*
 * Copyright 2021 Neural Layer
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

import org.icepear.echarts.Option
import org.icepear.echarts.charts.heatmap.HeatmapSeries
import org.icepear.echarts.components.title.Title
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.Config
import org.roboquant.common.round
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.getName
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.*
import kotlin.math.absoluteValue

class CalendarChart(
    private val metricsData: List<MetricsEntry>,
    private val fractionDigits: Int = 2,
    private val zoneId: ZoneId = Config.defaultZoneId
) : Chart() {

    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId)

    private fun prepData(): Map<Int, List<Any>> {
        val perYear = metricsData.groupBy { it.info.time.atZone(zoneId).year }
        val result = mutableMapOf<Int, List<Pair<String, BigDecimal>>>()
        perYear.forEach { (t, u) ->
            result[t] = u.map {
                Pair(timeFormatter.format(it.info.time), it.value.round(fractionDigits))
            }
        }
        return result.toSortedMap()
    }

    private fun getCalendars(data: Map<Int, List<Any>>): Array<Any> {
        var top = 100
        val result = mutableListOf<Any>()
        data.keys.forEach {
            result += mapOf(
                "top" to top,
                "range" to it,
                "cellSize" to Pair("auto", 20),
                "right" to 10
            )

            top += 200
        }

        // Ensure the height of the div is enough to show all calendars
        height = top
        return result.toTypedArray()
    }


    private fun getSeriesOptions(data: Map<Int, List<Any>>): Array<HeatmapSeries> {
        val seriesOptions = mutableListOf<HeatmapSeries>()
        data.values.forEachIndexed { index, list ->
            val hp = HeatmapSeries()
                .setData(list)
                .setCoordinateSystem("calendar")
                .setCalendarIndex(index)
            seriesOptions.add(hp)
        }
        return seriesOptions.toTypedArray()
    }


    private fun getTooltip(): Tooltip {
        return Tooltip()
            .setPosition("top")
            .setFormatter(javasciptFunction(
                "var f = echarts.format.formatTime('yyyy-MM-dd', p.data[0]);return f + ': ' + p.data[1];"
            ))
    }

    /** @suppress */
    override fun renderOption(): String {
        val data = prepData()
        val max = metricsData.map { it.value }.maxOfOrNull { it.absoluteValue } ?: 1.0

        val option = Option()
            .setTitle(Title().setText("Daily results ${metricsData.getName()}"))
            .setSeries(getSeriesOptions(data))
            .setCalendar(getCalendars(data))
            .setVisualMap(getVisualMap(-max, max))
            .setTooltip(getTooltip())
            .setToolbox(getBasicToolbox())

        return renderJson(option)
    }
}


