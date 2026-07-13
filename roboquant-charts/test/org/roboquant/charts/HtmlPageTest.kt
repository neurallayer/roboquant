package org.roboquant.charts

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.avro.AvroFeed
import org.roboquant.common.Stock
import org.roboquant.feeds.assets
import org.roboquant.feeds.filter
import org.roboquant.journals.MemoryJournal
import org.roboquant.journals.metrics.AccountMetric
import org.roboquant.run
import org.roboquant.strategies.EMACrossover
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class HtmlPageTest {

    @TempDir
    lateinit var folder: File

    @Test
    fun testRender() {
        val page = HtmlPage()
        val data = TestData.timeSeriesData
        page.addChart(BoxChart(data))
        page.addChart(CalendarChart(data))
        val file = File(folder, "test.html")
        assertDoesNotThrow {
            page.render(file.path)
        }
        assertTrue { file.exists() }
        assertTrue { file.isFile }
    }

    @Test
    fun testRender2() {
        val page = HtmlPage()
        page.theme = "dark"
        page.style = """
            body {
                background-color: #555;
            }
            .chart {
                background-color: black;
                margin: 30px 10px;
            }
        """.trimIndent()

        val feed = AvroFeed.sp25()
        val assets = feed.assets()
        val journal = MemoryJournal(AccountMetric())
        val account = run(feed, EMACrossover(), journal = journal)
        val ts = journal.getMetric("account.equity")

        page.addChart(BoxChart(ts))
        page.addChart(CalendarChart(ts, height = 900))
        page.addChart(PriceChart(feed, assets[0]))
        page.addChart(PriceChart(feed, assets[1]))
        page.addChart(SignalChart(feed, EMACrossover()))
        page.addChart(TradeChart(account.trades))
        page.addChart(PerformanceChart(feed))
        page.addChart(TimeSeriesChart(ts))
        page.addChart(CorrelationChart(feed, assets))
        page.addChart(HistogramChart(ts))
        assertDoesNotThrow {
            page.render("/tmp/test.html")
        }
    }

}