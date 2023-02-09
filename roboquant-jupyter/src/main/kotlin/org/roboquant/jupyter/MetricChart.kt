/*
 * Copyright 2020-2022 Neural Layer
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
import org.roboquant.loggers.getName
import org.roboquant.loggers.group
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A MetricChart will plot a metric that is captured during one or more runs.
 *
 * @property metricsData the data to use
 * @property useTime should be X axis be a timescale (or a step scale)
 * @property fractionDigits how many digits to use for presenting the metric values
 */
class MetricChart(
    private val metricsData: Collection<MetricsEntry>,
    private val useTime: Boolean = true,
    private val fractionDigits: Int = 2
) : Chart() {


    /**
     * Identify common suffix (same run/phase), so they can be removed from the series name
     */
    private fun commonSuffix(keys: Set<String>) : String {
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
            .setTitle(title ?: metricsData.getName())
            .addXAxis(xAxis)
            .addYAxis(yAxis)
            .setTooltip("axis")

        // Every combination of a run and metric name will be its own series
        val series = metricsData.group()
        val suffix = commonSuffix(series.keys)
        series.forEach { (name, entries) ->
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
     * @return
     */
    private fun List<MetricsEntry>.toSeriesData(): List<Pair<Any, Number>> {

        val d = mutableListOf<Pair<Any, Number>>()
        for (entry in this) {
            val value = entry.value
            if (value.isFinite()) {
                val roundedValue = BigDecimal(value).setScale(fractionDigits, RoundingMode.HALF_DOWN)
                if (useTime)
                    d.add(Pair(entry.info.time, roundedValue))
                else
                    d.add(Pair(entry.info.step.toString(), roundedValue))
            }
        }
        return d
    }

}