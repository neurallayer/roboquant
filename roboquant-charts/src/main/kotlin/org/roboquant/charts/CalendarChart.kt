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

import org.icepear.echarts.Option
import org.icepear.echarts.charts.heatmap.HeatmapSeries
import org.icepear.echarts.components.title.Title
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.TimeSeries
import org.roboquant.common.flatten
import org.roboquant.common.round
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.collections.*

/**
 * Plots the time-series values on a calendar, to visualize if there are days when there are outliers. Next
 * to the calendar, a visual map is being shown that allows filtering days based on the selected range of values.
 *
 * @param timeSeries the data to use
 * @property fractionDigits the number of digits to use, default is 2
 * @property zoneId the time zone to use to determine what day of the year it is, default is UTC
 */
class CalendarChart(
    timeSeries: TimeSeries,
    private val fractionDigits: Int = 2,
    private val zoneId: ZoneId = ZoneId.of("UTC")
) : Chart() {

    /**
     * Create a calendar chart from multiple runs. The data will be flattened before plotted.
     */
    constructor(metricsData: Map<String, TimeSeries>, fractionDigits: Int = 2, zoneId: ZoneId = ZoneOffset.UTC) :
            this(metricsData.flatten(true), fractionDigits, zoneId)

    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(zoneId)
    private val metricsData = timeSeries.filter { it.value.isFinite() }

    private fun prepData(): Map<Int, List<Pair<String, BigDecimal>>> {
        val perYear = metricsData.groupBy { it.time.atZone(zoneId).year }
        val result = mutableMapOf<Int, List<Pair<String, BigDecimal>>>()
        perYear.forEach { (t, u) ->
            // If there is more than one value per day, sum them together.
            val summed =
                u.groupBy { timeFormatter.format(it.time) }.mapValues { it.value.sumOf { entry -> entry.value } }
            result[t] = summed.mapValues { it.value.round(fractionDigits) }.toList()
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
            .setFormatter(
                javascriptFunction(
                    "var f = echarts.format.formatTime('yyyy-MM-dd', p.data[0]);return f + ': ' + p.data[1];"
                )
            )
    }

    /** @suppress */
    override fun getOption(): Option {
        val data = prepData()
        val max = metricsData.map { it.value }.maxOfOrNull { it }
        val min = metricsData.map { it.value }.minOfOrNull { it }

        return Option()
            .setTitle(Title().setText(title))
            .setSeries(getSeriesOptions(data))
            .setCalendar(getCalendars(data))
            .setVisualMap(getVisualMap(min, max))
            .setTooltip(getTooltip())
            .setToolbox(getBasicToolbox())
    }
}


