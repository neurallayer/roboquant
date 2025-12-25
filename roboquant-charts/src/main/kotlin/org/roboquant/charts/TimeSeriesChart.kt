/*
 * Copyright 2020-2026 Neural Layer
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

import org.icepear.echarts.Line
import org.icepear.echarts.Option
import org.icepear.echarts.charts.line.LineSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.series.LineStyle
import org.roboquant.common.TimeSeries
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A TimeSeriesChart will plot a metric that is captured during one or more runs. If there is more than one time-series
 * found in the provided [data], each time-series will be plotted as a separate colored line.
 *
 * @property data the time series data to use
 * @property useTime use a linear timescale for the x-axis.
 * @property fractionDigits how many digits to use for presenting the metric values
 */
class TimeSeriesChart(
    private val data: Map<String, TimeSeries>,
    private val useTime: Boolean = true,
    private val fractionDigits: Int = 2
) : Chart() {

    /**
     * Plot a single time-series
     */
    constructor(timeSeries: TimeSeries, useTime: Boolean = true, fractionDigits: Int = 2) :
            this(mapOf("" to timeSeries), useTime, fractionDigits)


    /**
     * Identify common suffix (same run), so they can be removed from the series name
     */
    private fun commonSuffix(keys: Set<String>): String {
        if (keys.isEmpty()) return ""
        val parts = keys.first().split("/")

        for (i in parts.lastIndex downTo 1) {
            val suffix = parts.takeLast(i).joinToString("/")
            if (keys.all { it.endsWith(suffix) }) return "/$suffix"
        }
        return ""
    }

    /** @suppress */
    override fun getOption(): Option {

        val xAxis = if (useTime) TimeAxis() else ValueAxis()
        val yAxis = ValueAxis().setScale(true)

        // Deal with many series on a single chart
        val showSymbol = data.size > 5
        val tooltip = if (showSymbol) "item" else "axis"

        val chart = Line()
            .setTitle(title)
            .addXAxis(xAxis)
            .addYAxis(yAxis)
            .setTooltip(tooltip)

        // Every combination of a run and metric name will be its own series
        val suffix = commonSuffix(data.keys)
        data.forEach { (name, entries) ->
            val d = reduce(entries.toSeriesData())
            val lineSeries = LineSeries()
                .setData(d)
                .setShowSymbol(showSymbol)
                .setName(name.removeSuffix(suffix))
                .setLineStyle(LineStyle().setWidth(1))

            chart.addSeries(lineSeries)
        }

        val option = chart.option
        option.setToolbox(getToolbox())
        option.setDataZoom(DataZoom())

        return option
    }

    /**
     * Convert a list of entries to data-format suitable for chart series.
     */
    private fun TimeSeries.toSeriesData(): List<Pair<Long, BigDecimal>> {

        val d = mutableListOf<Pair<Long, BigDecimal>>()
        for ((step, entry) in withIndex()) {
            val value = entry.value
            if (value.isFinite()) {
                val y = BigDecimal(value).setScale(fractionDigits, RoundingMode.HALF_DOWN)
                val x = if (useTime) entry.time.toEpochMilli() else step.toLong()
                d.add(Pair(x, y))
            }
        }
        return d
    }

}
