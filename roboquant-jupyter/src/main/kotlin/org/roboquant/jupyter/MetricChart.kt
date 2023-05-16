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
import org.roboquant.loggers.MetricsEntry
import org.roboquant.loggers.flatten
import org.roboquant.loggers.perc
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A MetricChart will plot a metric that is captured during one or more runs. If there is more than one run found in
 * the provided [metricsData], each run will be plotted as a separate series.
 *
 * @property metricsData the metric data to use
 * @property useTime should be X axis be a timescale (or a step scale)
 * @property fractionDigits how many digits to use for presenting the metric values
 */
class MetricChart(
    private val metricsData: Map<String,List<MetricsEntry>>,
    private val useTime: Boolean = true,
    private val fractionDigits: Int = 2
) : Chart() {

    /**
     * Plot a single run
     */
    constructor(metricsData: List<MetricsEntry>, useTime: Boolean=true, fractionDigits: Int =2) :
            this(mapOf("" to metricsData), useTime, fractionDigits)


    companion object {

        /**
         * Create a chart based on a metric for a number of recorded walk forward runs. Because at the start of each
         * walk forward the metric might be reset, the metric is first converted to percentages.
         */
        fun walkForward(metricsData: Map<String,List<MetricsEntry>>, fractionDigits: Int = 2): MetricChart {
            val d = metricsData.perc().flatten()
            var v = 100.0
            val data = d.map {
                v *= (1.0 + it.value / 100.0)
                MetricsEntry(v, it.time)
            }
            return MetricChart(data, true, fractionDigits)
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
        val suffix = commonSuffix(metricsData.keys)
        metricsData.forEach { (name, entries) ->
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
    private fun List<MetricsEntry>.toSeriesData(): List<Pair<Long, BigDecimal>> {

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