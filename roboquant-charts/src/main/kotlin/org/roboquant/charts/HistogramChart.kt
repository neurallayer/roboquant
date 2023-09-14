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

import org.hipparchus.stat.fitting.EmpiricalDistribution
import org.icepear.echarts.Bar
import org.icepear.echarts.Option
import org.icepear.echarts.charts.bar.BarLabel
import org.icepear.echarts.charts.bar.BarSeries
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.TimeSeries
import org.roboquant.common.clean
import org.roboquant.common.flatten
import java.math.BigDecimal
import java.math.RoundingMode


/**
 * Chart that takes [timeSeries] and creates a histogram of it. The number of [binCount] to display is configurable.
 *
 * @property timeSeries
 * @property binCount
 * @property scale
 * @property minBinSize
 * @constructor Create empty Metric histogram
 */
class HistogramChart(
    private val timeSeries: TimeSeries,
    private val binCount: Int = 20,
    private val scale: Int = 2,
    private val minBinSize: Int = 0,
) : Chart() {

    constructor(
        metricData: Map<String, TimeSeries>,
        binCount: Int = 20,
        scale: Int = 2,
        minBinSize: Int = 0,
    ) : this(metricData.flatten(true), binCount, scale, minBinSize)

    private fun toSeriesData(): List<Pair<String, Long>> {
        val f = EmpiricalDistribution(binCount)
        val data = timeSeries.toDoubleArray().clean()
        if (data.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<String, Long>>()
        f.load(data)
        var binSize = 0L
        for (i in 0 until binCount) {
            val roundedValue = BigDecimal(f.upperBounds[i]).setScale(scale, RoundingMode.HALF_DOWN)
            binSize += f.binStats[i].n
            val last = i == binCount - 1
            if (binSize >= minBinSize || (last && binSize > 0)) {
                val e = Pair("$roundedValue", binSize)
                result.add(e)
                binSize = 0
            }
        }
        return result
    }

    /** @suppress */
    override fun getOption(): Option {
        val d = toSeriesData()
        val data = d.map { it.second }.toTypedArray()
        val xData = d.map { it.first }.toTypedArray()

        val series = BarSeries()
            .setBarWidth("70%")
            .setData(data)
            .setLabel(BarLabel().setShow(true))

        val chart = Bar()
            .setTitle(title)
            .addYAxis(ValueAxis().setScale(true))
            .addXAxis(CategoryAxis().setData(xData).setAxisTick(mapOf("alignWithLabel" to true)))
            .addSeries(series)
            .setTooltip(Tooltip().setTrigger("axis"))

        val option = chart.option
        option.setToolbox(getToolbox())
        option.setDataZoom(DataZoom())

        return option
    }
}
