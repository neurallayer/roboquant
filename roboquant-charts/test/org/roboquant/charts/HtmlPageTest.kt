package org.roboquant.charts

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.io.File
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

}