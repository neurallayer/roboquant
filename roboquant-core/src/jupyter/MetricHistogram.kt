package org.roboquant.jupyter

import org.apache.commons.math3.random.EmpiricalDistribution
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.clean
import org.roboquant.logging.getName
import org.roboquant.logging.toDoubleArray
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Metric histogram
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
        if (data.isEmpty()) return listOf()

        f.load(data)
        for (i in 0 until binCount) {
            val roundedValue = BigDecimal(f.upperBounds[i]).setScale(scale, RoundingMode.HALF_DOWN)
            val e = Pair("$roundedValue", f.binStats[i].n)
            result.add(e)
        }
        return result
    }

    override fun renderOption(): String {
        val gson = gsonBuilder.create()
        val d = toSeriesData()
        val data = gson.toJson(d.map { it.second })
        val xData = gson.toJson(d.map { it.first })

        return """
            {
                xAxis: {
                    type: 'category',
                    scale: true,
                    data: $xData,
                    axisTick: {
                        alignWithLabel: true
                    }
                },
                yAxis: {
                    type: 'value',
                    scale: true
                },
                 title: {
                    text: 'Histogram ${metricData.getName()}'
                },
               tooltip: {
                    trigger: 'axis',
                    axisPointer: {           
                        type: 'shadow'      
                    }
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},  
                series : {
                    name: '${metricData.getName()}',
                    type: 'bar',
                    barWidth: '70%',
                    label: { show: true },
                    data : $data
                }
            }
       """
    }
}