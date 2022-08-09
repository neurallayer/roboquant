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

import org.apache.commons.math3.random.EmpiricalDistribution
import org.icepear.echarts.Bar
import org.icepear.echarts.Option
import org.icepear.echarts.charts.bar.BarLabel
import org.icepear.echarts.charts.bar.BarSeries
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.logging.MetricsEntry
import org.roboquant.common.clean
import org.roboquant.logging.getName
import org.roboquant.logging.toDoubleArray
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Chart that takes [metricData] and creates a histogram of it. The number of [binCount] to display is configurable.
 *
 * @property metricData
 * @property binCount
 * @property scale
 * @constructor Create empty Metric histogram
 */
class MetricHistogram(
    private val metricData: Collection<MetricsEntry>,
    private val binCount: Int = 20,
    private val scale: Int = 2
) : Chart() {

    private fun toSeriesData(): List<Pair<String, Long>> {
        val result = mutableListOf<Pair<String, Long>>()
        val f = EmpiricalDistribution(binCount)
        val data = metricData.toDoubleArray().clean()
        if (data.isEmpty()) return emptyList()

        f.load(data)
        for (i in 0 until binCount) {
            val roundedValue = BigDecimal(f.upperBounds[i]).setScale(scale, RoundingMode.HALF_DOWN)
            val e = Pair("$roundedValue", f.binStats[i].n)
            result.add(e)
        }
        return result
    }

    /** @suppress */
    override fun getOption(): Option {
        val d = toSeriesData()
        val data = d.map { it.second }.toTypedArray()
        val xData = d.map { it.first }.toTypedArray()

        val series = BarSeries()
            .setName(metricData.getName())
            .setBarWidth("70%")
            .setData(data)
            .setLabel(BarLabel().setShow(true))

        val chart = Bar()
            .setTitle(title ?: "Metric: ${metricData.getName()}")
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