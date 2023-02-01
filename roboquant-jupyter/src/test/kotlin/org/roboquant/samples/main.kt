package org.roboquant.samples

import org.jetbrains.kotlinx.jupyter.joinToLines
import org.roboquant.Roboquant
import org.roboquant.feeds.AvroFeed
import org.roboquant.jupyter.*
import org.roboquant.loggers.MetricsLogger
import org.roboquant.loggers.diff
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ScorecardMetric
import org.roboquant.strategies.EMAStrategy

private class HTMLReport(
    private val charts : List<Chart> = emptyList(),
    private val logger: MetricsLogger? = null
) : HTMLOutput (
) {

    private fun metricsToHTML(): String {
        if (logger == null) return ""
        val metrics = logger.metricNames.map { logger.getMetric(it).last() }
        val rows = metrics.map { """
            <tr>
                <td>${it.name}</td>                        
                <td>${it.value}</td>  
            </tr>""".trimIndent() }.joinToLines()

        return "<table>$rows</table>\n"
    }

    private fun chartsToHTML(): String {
        return charts.map {
            "<div>${it.asHTML()}</div>"
        }.joinToLines()
    }


    override fun asHTML(): String {
        return """
            ${metricsToHTML()}
            ${chartsToHTML()}
        """.trimIndent()
    }

    override fun asHTMLPage(): String {
        return """
            <html>
            <head>
            ${Chart.getScript()}
            </head>
            <body>
            ${asHTML()}
            </body>
            </html>
           
        """.trimIndent()
    }

}

fun main() {
    val rq = Roboquant(EMAStrategy(), ScorecardMetric(), AccountMetric())
    val feed = AvroFeed.sp500()
    rq.run(feed)
    val equity = rq.logger.getMetric("account.equity")
    val chart1 = MetricChart(equity)
    chart1.height = 1000

    val chart2 = TradeChart(rq.broker.account.trades)
    chart2.height = 1000

    val chart3 = MetricCalendarChart(equity.diff())
    chart3.height = 1000

    val charts = listOf(chart1, chart2, chart3)
    val report = HTMLReport(charts, rq.logger)
    report.toHTMLFile("/tmp/test.html")
}