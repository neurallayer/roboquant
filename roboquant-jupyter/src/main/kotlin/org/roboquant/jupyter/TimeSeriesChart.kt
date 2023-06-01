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

import org.icepear.echarts.Line
import org.icepear.echarts.Option
import org.icepear.echarts.charts.line.LineSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.series.LineStyle
import org.roboquant.common.TimeSeries
import org.roboquant.common.flatten
import java.math.BigDecimal
import java.math.RoundingMode


@Deprecated("Renamed to TimeSeriesChart", ReplaceWith("TimeSeriesChart", "org.roboquant.jupyter.TimeSeriesChart"))
typealias MetricChart = TimeSeriesChart

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
     * @suppress
     */
    companion object {

        /**
         * Return a chart based on multiple runs. Because each run starts fresh, not the absolute values, but the
         * returns are used to flatten runs to a single timeline.
         *
         * ```
         * val data = roboquant.logger.getMetric("account.equity")
         * TimeSeriesChart.walkForward(data, monteCarlo = true)
         * ```
         */
        fun walkForward(
            metricsData: Map<String, TimeSeries>,
            fractionDigits: Int = 2,
            monteCarlo: Int = 0
        ): TimeSeriesChart {
            require(monteCarlo >= 0)
            require(fractionDigits >= 0)

            val d = metricsData.mapValues { it.value.returns() }.flatten()
            var data = d.runningFold(100.0)
            if (monteCarlo == 0) return TimeSeriesChart(mapOf("" to data))

            val result = mutableMapOf("orginal" to data)
            repeat(monteCarlo) {
                data = d.shuffle().runningFold(100.0)
                result["mc-$it"] = data
            }
            return TimeSeriesChart(result)

        }

    }

    /**
     * Identify common suffix (same run/phase), so they can be removed from the series name
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

        val chart = Line()
            .setTitle(title)
            .addXAxis(xAxis)
            .addYAxis(yAxis)
            .setTooltip("axis")

        // Every combination of a run and metric name will be its own series
        val suffix = commonSuffix(data.keys)
        data.forEach { (name, entries) ->
            val d = reduce(entries.toSeriesData())
            val lineSeries = LineSeries()
                .setData(d)
                .setShowSymbol(false)
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