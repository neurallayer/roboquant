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

import org.roboquant.common.plusAssign
import org.roboquant.logging.MetricsEntry
import org.roboquant.logging.getName
import org.roboquant.logging.group
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Metric chart will plot a metric
 *
 * @property metricsData
 * @property useTime
 */
class MetricChart(
    private val metricsData: Collection<MetricsEntry>,
    private val useTime: Boolean = true,
    private val fractionDigits: Int = 2
) : Chart() {

    /** @suppress */
    override fun renderOption(): String {

        // Every combination of a run and episode will be its own series
        val series = metricsData.group()
        val result = StringBuffer()
        val gson = gsonBuilder.create()
        series.forEach { (name, entries) ->
            val d = reduce(entries.toSeriesData())
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
                    text: 'Metric: ${metricsData.getName()}'
                },
                tooltip: {
                    trigger: 'axis'
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},
                series : [$result]
            }"""
    }


    /**
     * Convert a list of entries to data-format suitable for chart series.
     * @return
     */
    private fun List<MetricsEntry>.toSeriesData(): List<Pair<Any, Number>> {

        val d = mutableListOf<Pair<Any, Number>>()
        for (entry in this) {
            val value = entry.value
            val roundedValue = BigDecimal(value).setScale(fractionDigits, RoundingMode.HALF_DOWN)
            if (useTime)
                d.add(Pair(entry.info.time, roundedValue))
            else
                d.add(Pair(entry.info.step.toString(), roundedValue))

        }
        return d
    }


}