/*
 * Copyright 2020-2024 Neural Layer
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

import org.hipparchus.stat.descriptive.rank.Percentile
import org.icepear.echarts.Boxplot
import org.icepear.echarts.Option
import org.icepear.echarts.charts.boxplot.BoxplotSeries
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.roboquant.common.TimeSeries
import org.roboquant.common.clean
import java.math.MathContext
import java.math.RoundingMode
import java.time.temporal.ChronoUnit

/**
 * A box chart is a standardized way of displaying data based on: the minimum, the maximum, and the
 * low-, mid- and high percentiles. It provides a good indication how the values of a certain metric are distributed
 * during a certain [period].
 *
 * @property timeSeries the metric data to use
 * @property period the period to use for one box to calculate percentiles
 * @property lowPercentile the low percentile, default is 25.0
 * @property midPercentile the mid-percentile, default is 50.0
 * @property highPercentile the high percentile, default is 75.0
 * @property precision the precision to use, default is 8
 */
class BoxChart(
    private val timeSeries: TimeSeries,
    private val period: ChronoUnit = ChronoUnit.MONTHS,
    private val lowPercentile: Double = 25.0,
    private val midPercentile: Double = 50.0,
    private val highPercentile: Double = 75.0,
    private val precision: Int = 8
) : Chart() {


    private fun toSeriesData(): List<Pair<String, Any>> {
        val result = mutableListOf<Pair<String, Any>>()
        val ctx = MathContext(precision, RoundingMode.HALF_DOWN)
        for (d in timeSeries.groupBy(period)) {
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
            .setData(yData)

        val chart = Boxplot()
            .setTitle(title)
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
