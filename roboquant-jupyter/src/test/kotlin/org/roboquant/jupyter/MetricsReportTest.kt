package org.roboquant.jupyter


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.Roboquant
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import java.io.File
import kotlin.test.assertTrue

internal class MetricsReportTest {

    @TempDir
    lateinit var folder: File

    @Test
    fun test() {
        val f = RandomWalkFeed.lastYears(1, 1, generateBars = true)
        val rq = Roboquant(EMAStrategy(), AccountMetric())
        assertDoesNotThrow {
            val report = MetricsReport(rq)
            val file = File(folder, "test.html")
            report.toHTMLFile(file.toString())
            assertTrue(file.exists())
        }

        rq.run(f)
        assertDoesNotThrow {
            val report = MetricsReport(rq)
            val file = File(folder, "test.html")
            report.toHTMLFile(file.toString())
            assertTrue(file.exists())
        }
    }


}