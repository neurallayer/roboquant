package org.roboquant.jupyter

import org.apache.commons.math3.stat.descriptive.rank.Percentile
import org.roboquant.common.max
import org.roboquant.common.min
import org.roboquant.logging.*

/**
 * A box chart is a standardized way of displaying data based on: the minimum, the maximum, and the
 * low-, mid- and high percentiles. It provides a good indication how a certain metric is distributed over a certain
 * period of time.
 */
class MetricBoxChart(
    private val metricData: Collection<MetricsEntry>,
    val period: String = "month",
    private val lowPercentile: Double = 25.0,
    private val midPercentile: Double = 50.0,
    private val highPercentile: Double = 75.0,
) : Chart() {

    private fun toSeriesData(): List<Pair<String, DoubleArray>> {
        val result = mutableListOf<Pair<String, DoubleArray>>()
        for (d in metricData.groupBy(period)) {
            val arr = d.value.toDoubleArray().clean()
            if (arr.isNotEmpty()) {
                val p = Percentile()
                p.data = arr
                val tmp = doubleArrayOf(
                    arr.min(),
                    p.evaluate(lowPercentile),
                    p.evaluate(midPercentile),
                    p.evaluate(highPercentile),
                    arr.max()
                )
                result.add(Pair(d.key, tmp))
            }
        }
        result.sortBy { it.first }
        return result
    }

    /** @suppress */
    override fun renderOption(): String {
        val gson = gsonBuilder.create()
        val d = toSeriesData()
        val data = gson.toJson(d.map { it.second })
        val xData = gson.toJson(d.map { it.first })

        return """
            {
                title: {
                        text: 'BoxPlot ${metricData.getName()}',
                    },
                ${renderDataZoom()},
                ${renderToolbox(false)},
                ${renderGrid()},
                xAxis: {
                    type: 'category',
                    data: $xData
                },
                yAxis: {
                    type: 'value',
                    splitArea: {
                        show: true
                    }
                },
                series: [
                    {
                        name: 'boxplot',
                        type: 'boxplot',
                        data : $data
                    }
                ]
            };
        """.trimIndent()
    }
}