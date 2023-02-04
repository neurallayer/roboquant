package org.roboquant.samples

import org.jetbrains.kotlinx.jupyter.joinToLines
import org.roboquant.Roboquant
import org.roboquant.brokers.lines
import org.roboquant.common.round
import org.roboquant.feeds.AvroFeed
import org.roboquant.jupyter.*
import org.roboquant.loggers.MetricsEntry
import org.roboquant.loggers.diff
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.orders.lines
import org.roboquant.strategies.EMAStrategy
import java.nio.charset.StandardCharsets

private class HTMLReport(
    private val roboquant: Roboquant,
) : HTMLOutput() {

    val logger
        get() = roboquant.logger

    val charts
        get() = logger.metricNames.map {
            { MetricChart(roboquant.logger.getMetric(it)) }
        } + logger.metricNames.map {
            { MetricCalendarChart(roboquant.logger.getMetric(it).diff()) }
        } + logger.metricNames.map {
            { MetricHistogramChart(roboquant.logger.getMetric(it)) }
        }

        /**
            { MetricChart(roboquant.logger.getMetric("scorecard.equity")) },
            { MetricChart(roboquant.logger.getMetric("scorecard.mdd")) },
            { MetricChart(roboquant.logger.getMetric("scorecard.winners")) },
            { MetricChart(roboquant.logger.getMetric("scorecard.losers")) },
            { MetricChart(roboquant.logger.getMetric("scorecard.profit.realized")) },
            { TradeChart(roboquant.broker.account.trades) },
            { MetricCalendarChart(roboquant.logger.getMetric("scorecard.equity").diff()) },
            { MetricCalendarChart(roboquant.logger.getMetric("scorecard.winners").diff()) },
            { MetricCalendarChart(roboquant.logger.getMetric("scorecard.losers").diff()) },
        )
            **/

    private fun getTableCell(entry: MetricsEntry): String {
        val splittedName = entry.name.split('.')
        val name = splittedName.subList(1, splittedName.size).joinToString(" ")
        val value = entry.value.round(2).toString().removeSuffix(".00")
        return "<td>$name</td><td align=right>$value</td>"
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
            result += "</table></div>"
        }
        return result.toString()
    }

    private fun linesToHTML(lines: List<List<Any>>, caption: String): String {
        val result = StringBuffer()
        val style = "overflow: scroll; max-height:700px; flex: 33%;"
        result +=
            "<div class='flex-item' style='$style'><table frame=void rules=rows class=table><caption>$caption</caption>"
        val header = lines.first()
        result += "<tr>"
        for (column in header) {
            result += "<th>$column</th>"
        }
        result += "</tr>"
        lines.subList(1, lines.size).forEach {
            result += "<tr>"
            for (column in it) {
                result += "<td>$column</td>"
            }
            result += "</tr>"
        }
        result += "</table></div>"
        return result.toString()
    }


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
        val account = roboquant.broker.account
        val orders = account.closedOrders + account.openOrders
        return """
             <div class="flex-container">
                 <h2>Metrics</h2>
                 ${metricsToHTML()}
             </div>
             <div class="flex-container">
                <h2>Account</h2>
                ${linesToHTML(account.trades.lines(), "Trades")}
                ${linesToHTML(orders.lines(), "Orders")}
                ${linesToHTML(account.positions.lines(), "Positions")}
            </div>
            <div class="flex-container">
                <h2>Charts</h2>
                ${chartsToHTML()}
            </div>
        """.trimIndent()
    }


    fun loadStyle() : String {
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
    val rq = Roboquant(EMAStrategy(), ScorecardMetric())
    val feed = AvroFeed.sp500()
    rq.run(feed)
    val report = HTMLReport(rq)
    report.toHTMLFile("/tmp/test.html")
}