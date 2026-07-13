package org.roboquant.charts

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.avro.AvroFeed
import org.roboquant.feeds.assets
import org.roboquant.journals.MemoryJournal
import org.roboquant.journals.metrics.AccountMetric
import org.roboquant.run
import org.roboquant.strategies.EMACrossover
import java.io.File
import java.nio.file.Paths
import java.time.temporal.ChronoUnit
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
        val page = HtmlPage(darkTheme = true)
        val feed = AvroFeed.sp25()
        val assets = feed.assets()
        val journal = MemoryJournal(AccountMetric())
        val account = run(feed, EMACrossover(), journal = journal)
        val ts = journal.getMetric("account.equity")

        page.addChart(BoxChart(ts, ChronoUnit.YEARS))
        page.addChart(CalendarChart(ts, height = 900))
        page.addChart(PriceChart(feed, assets[0]))
        page.addChart(PriceChart(feed, assets[1]))
        page.addChart(SignalChart(feed, EMACrossover()))
        page.addChart(TradeChart(account.trades, dropZeroPNL = true))
        page.addChart(PerformanceChart(feed))
        page.addChart(TimeSeriesChart(ts))
        page.addChart(CorrelationChart(feed, assets))
        page.addChart(HistogramChart(ts))

        val tmpdir = System.getProperty("java.io.tmpdir")
        val fileName = Paths.get(tmpdir, "test.html").toString()
        assertDoesNotThrow {
            page.render(fileName)
        }
        println("***** generated HTML file at: $fileName")
    }

}