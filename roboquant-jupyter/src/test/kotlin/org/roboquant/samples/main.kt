package org.roboquant.samples

import org.jetbrains.kotlinx.jupyter.joinToLines
import org.roboquant.Roboquant
import org.roboquant.common.Config
import org.roboquant.common.round
import org.roboquant.feeds.AvroFeed
import org.roboquant.jupyter.Chart
import org.roboquant.jupyter.HTMLOutput
import org.roboquant.jupyter.MetricChart
import org.roboquant.loggers.MetricsEntry
import org.roboquant.metrics.ReturnsMetric2
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.strategies.EMAStrategy
import java.nio.charset.StandardCharsets
import kotlin.io.path.div

private class Scorecard(
    private val roboquant: Roboquant,
) : HTMLOutput() {

    /**
    init {
       require(roboquant.metrics.any { it is ScorecardMetric })  { "No ScorecardMetric configured"}
    }
    */

    val logger
        get() = roboquant.logger

    val charts
        get() = logger.metricNames.map {
            { MetricChart(roboquant.logger.getMetric(it)) }
        }

    private fun createCells(name: String, value: Any): String {
        return "<td>$name</td><td align=right>$value</td>"
    }

    private fun getTableCell(entry: MetricsEntry): String {
        val splittedName = entry.name.split('.')
        val name = splittedName.subList(1, splittedName.size).joinToString(" ")
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

    /*
    private fun linesToHTML(lines: List<List<Any>>, caption: String): String {
        val result = StringBuffer()
        val style = "overflow: scroll; max-height:700px; flex: 33%;"
        result +=
            "<div class='flex-item' style='$style'><table frame=void rules=rows class=table><caption>$caption</caption>"
        val header = lines.first()
        result += "<thead><tr>"
        for (column in header) {
            result += "<th>$column</th>"
        }
        result += "</tr></thead><tbody>"
        lines.subList(1, lines.size).forEach {
            result += "<tr>"
            for (column in it) {
                result += "<td>$column</td>"
            }
            result += "</tr>"
        }
        result += "</tbody></table></div>"
        return result.toString()
    }
    */



    private fun chartsToHTML(): String {
        return charts.map {
            """<div class="flex-item" style="flex: 700px;">
                    <div class="chart">
                    ${it().asHTML()}
                    </div>
               </div>""".trimIndent()
        }.joinToLines()
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

    private fun loadStyle() : String {
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

}

private operator fun StringBuffer.plusAssign(s: String) {
    this.append(s)
}

fun main() {
    Chart.maxSamples = 1_000
    val rq = Roboquant(
        EMAStrategy(),
        ReturnsMetric2(),
        ScorecardMetric()
    )
    val path = Config.home / "all_1962_2023.avro"
    val feed = AvroFeed(path)
    rq.run(feed)
    val report = Scorecard(rq)
    report.toHTMLFile("/tmp/test.html")
    println(rq.broker.account.summary())
}