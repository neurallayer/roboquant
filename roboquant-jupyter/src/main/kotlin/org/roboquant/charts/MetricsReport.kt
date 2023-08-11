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

import org.roboquant.Roboquant
import org.roboquant.common.TimeSeries
import java.nio.charset.StandardCharsets

/**
 * Generate an HTML report that contains the recorded metrics of one or more runs. The report will contain both a
 * table with metric values and charts.
 *
 * @param maxSamples maximum samples per chart to use when creating charts.
 */
class MetricsReport(
    private val roboquant: Roboquant,
    maxSamples: Int = 10_000
) : HTMLOutput() {

    init {
        Chart.maxSamples = maxSamples
    }

    private val logger
        get() = roboquant.logger

    private val charts
        get() = logger.metricNames.map {
            { TimeSeriesChart(roboquant.logger.getMetric(it)) }
        }

    private fun createCells(name: String, value: Any): String {
        return "<td>$name</td><td align=right>$value</td>"
    }

    private fun getTableCell(entry: Map.Entry<String, TimeSeries>): String {
        val splitName = entry.key.split('.')
        val name = splitName.subList(1, splitName.size).joinToString(" ")
        // val value = if (entry.value.isFinite()) entry.value.round(2).toString().removeSuffix(".00") else "NaN"
        val value = 2.0
        return createCells(name, value)
    }


    private fun metricsToHTML(): String {
        val metricsMap = logger.metricNames.map { it to logger.getMetric(it) }
        val result = StringBuffer()
        for ((name, metrics) in metricsMap) {
            result += "<div class='flex-item'><table frame=void rules=rows class='table'><caption>$name</caption>"
            for (metric in metrics) {
                result += "<tr>"
                result += getTableCell(metric)
                result += "</tr>"
            }
            // result += "<tr>${createCells("start time", logger.getMetric(name).first().step.time)}</tr>"
            // result += "<tr>${createCells("end time", logger.getMetric(name).last().step.time)}</tr>"
            result += "</table></div>"
        }

        return result.toString()
    }


    private fun chartsToHTML(): String {
        return charts.joinToString {
            """<div class="flex-item" style="flex: 700px;">
                    <div class="chart">
                    ${it().asHTML()}
                    </div>
               </div>""".trimIndent()
        }
    }

    override fun asHTML(): String {
        return """
             <div class="flex-container">
                 <h2>Metrics</h2>
                 ${metricsToHTML()}
             </div>
            <div class="flex-container">
                <h2>Charts</h2>
                ${chartsToHTML()}
            </div>
        """.trimIndent()
    }

    private fun loadStyle(): String {
        val stream = this::class.java.getResourceAsStream("/css/report.css")!!
        return String(stream.readAllBytes(), StandardCharsets.UTF_8)
    }

    override fun asHTMLPage(): String {
        return """
            <!doctype html>
            <html lang="en">
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <style>
            ${loadStyle()}
            </style>
            ${Chart.getScript()}
            </head>
            <body>
            ${asHTML()}
            </body>
            </html>      
        """.trimIndent()
    }

    private operator fun StringBuffer.plusAssign(s: String) {
        this.append(s)
    }

}
