package org.roboquant.jupyter

import org.roboquant.common.plusAssign
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.getName
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Metric chart will visualize a metric
 *
 * @property metricsData
 * @property useTime
 * @constructor Create new metric chart
 */
class MetricChart(
    private val metricsData: Collection<MetricsEntry>,
    private val useTime: Boolean = true,
    private val scale: Int = 2
) : Chart() {

    /** @suppress */
    override fun renderOption(): String {

        // Every combination of a run and episode will be its own series
        val series = metricsData.groupBy { it.info.run.toString() + "/" + it.info.episode.toString() }
        val result = StringBuffer()
        val gson = gsonBuilder.create()
        series.forEach { (name, entries) ->
            val d = entries.toSeriesData()
            val data = gson.toJson(d)
            result += """
                {
                    name: '$name',
                    type: 'line',
                    showSymbol: false,
                    lineStyle: {
                        width: 1
                    },
                    data : $data
                },
            """
        }

        val xAxisType = if (useTime) "time" else "value"
        return """
            {
                xAxis: {
                    type: '$xAxisType',
                    scale: true
                },
                yAxis: {
                    type: 'value',
                    scale: true
                },
                 title: {
                    text: 'Metric ${metricsData.getName()}'
                },
                tooltip: {
                    trigger: 'axis'
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},  
                series : [$result]
            }
       """.trimStart()
    }


    /**
     * Convert a list of entries to data-format suitable for chart series.
     * @return
     */
    private fun List<MetricsEntry>.toSeriesData(): List<Pair<Any, Number>> {

        val d = mutableListOf<Pair<Any, Number>>()
        for (entry in this) {
            val value = entry.value
            val roundedValue = BigDecimal(value.toDouble()).setScale(scale, RoundingMode.HALF_DOWN)
            if (useTime)
                d.add(Pair(entry.info.time, roundedValue))
            else
                d.add(Pair(entry.info.step.toString(), roundedValue))

        }
        return d
    }


}