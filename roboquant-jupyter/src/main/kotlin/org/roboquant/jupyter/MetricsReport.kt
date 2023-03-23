package org.roboquant.jupyter

import org.roboquant.Roboquant
import org.roboquant.common.round
import org.roboquant.loggers.MetricsEntry
import java.nio.charset.StandardCharsets

/**
 * Generate an HTML report that contains the recorded metrics of one or more runs.
 *
 * @param maxSamples maximum samples prt chart to use when creating charts.
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
            { MetricChart(roboquant.logger.getMetric(it)) }
        }

    private fun createCells(name: String, value: Any): String {
        return "<td>$name</td><td align=right>$value</td>"
    }

    private fun getTableCell(entry: MetricsEntry): String {
        val splitName = entry.name.split('.')
        val name = splitName.subList(1, splitName.size).joinToString(" ")
        val value = if (entry.value.isFinite()) entry.value.round(2).toString().removeSuffix(".00") else "NaN"
        return createCells(name, value)
    }


    private fun metricsToHTML(): String {
        val metricsMap = logger.metricNames.map { logger.getMetric(it).last() }.groupBy { it.name.split('.').first() }
        val result = StringBuffer()
        for ((prefix, metrics) in metricsMap) {
            result += "<div class='flex-item'><table frame=void rules=rows class='table'><caption>$prefix</caption>"
            for (metric in metrics) {
                result += "<tr>"
                result += getTableCell(metric)
                result += "</tr>"
            }
            val name = logger.metricNames.first()
            result += "<tr>${createCells("start time", logger.getMetric(name).first().info.time)}</tr>"
            result += "<tr>${createCells("end time", logger.getMetric(name).last().info.time)}</tr>"
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
