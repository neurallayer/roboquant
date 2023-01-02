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

import org.hipparchus.stat.descriptive.rank.Percentile
import org.icepear.echarts.Boxplot
import org.icepear.echarts.Option
import org.icepear.echarts.charts.boxplot.BoxplotSeries
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.roboquant.common.clean
import org.roboquant.loggers.MetricsEntry
import org.roboquant.loggers.getName
import org.roboquant.loggers.groupBy
import org.roboquant.loggers.toDoubleArray
import java.math.MathContext
import java.math.RoundingMode
import java.time.temporal.ChronoUnit

/**
 * A box chart is a standardized way of displaying data based on: the minimum, the maximum, and the
 * low-, mid- and high percentiles. It provides a good indication how the values of a certain metric are distributed.
 */
class MetricBoxChart(
    private val metricData: Collection<MetricsEntry>,
    private val period: ChronoUnit = ChronoUnit.MONTHS,
    private val lowPercentile: Double = 25.0,
    private val midPercentile: Double = 50.0,
    private val highPercentile: Double = 75.0,
    private val precision: Int = 8
) : Chart() {

    private fun toSeriesData(): List<Pair<String, Any>> {
        val result = mutableListOf<Pair<String, Any>>()
        val ctx = MathContext(precision, RoundingMode.HALF_DOWN)
        for (d in metricData.groupBy(period)) {
            val arr = d.value.toDoubleArray().clean()
            if (arr.isNotEmpty()) {
                val p = Percentile()
                p.data = arr
                val entry = listOf(
                    arr.min().toBigDecimal(ctx),
                    p.evaluate(lowPercentile).toBigDecimal(ctx),
                    p.evaluate(midPercentile).toBigDecimal(ctx),
                    p.evaluate(highPercentile).toBigDecimal(ctx),
                    arr.max().toBigDecimal(ctx)
                )
                result.add(Pair(d.key, entry))
            }
        }
        result.sortBy { it.first }
        return result
    }

    /** @suppress */
    override fun getOption(): Option {
        val data = toSeriesData()
        val xData = data.map { it.first }.toTypedArray()
        val yData = data.map { it.second }

        val series = BoxplotSeries()
            .setName(metricData.getName())
            .setData(yData)

        val chart = Boxplot()
            .setTitle(title ?: "Metric: ${metricData.getName()}")
            .addSeries(series)
            .addYAxis(ValueAxis())
            .addXAxis(CategoryAxis().setData(xData))
            .setTooltip("axis")

        val option = chart.option
        option.setToolbox(getToolbox(false))
        option.setDataZoom(DataZoom())

        return option
    }
}