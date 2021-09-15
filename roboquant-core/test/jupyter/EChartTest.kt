package org.roboquant.jupyter

import org.junit.Test
import org.roboquant.feeds.random.RandomWalk
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class EChartTest {


    @Test
    fun test() {

        val f = RandomWalk.lastYears(1, 1, generateBars = true)
        val asset = f.assets.first()
        val chart = PriceBarChart(f, asset)
        assertTrue(chart.asHTML().isNotBlank())
        assertEquals(700, chart.height)
        assertContains(chart.asHTML(), asset.symbol)

        // val file = folder.newFile("test.html")
        // val file = tempDir.resolve("test.html")
        // chart.toHTMLFile(file.toString())
        // assertTrue(file.exists())
    }


}