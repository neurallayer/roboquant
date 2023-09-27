package org.roboquant.charts

import org.junit.jupiter.api.io.TempDir
import org.roboquant.feeds.random.RandomWalkFeed
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ImageCreatorTestIT {

    private val imageCreator = ImageCreator()
    private val feed = RandomWalkFeed.lastYears(1, nAssets = 5)

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
        val chart = PriceBarChart(feed, feed.assets.first())
        val svg = imageCreator.renderToSVGString(chart)
        assertTrue(svg.isNotEmpty())
    }

    @Test
    fun corr() {
        val chart = CorrelationChart(feed, feed.assets)
        val svg = imageCreator.renderToSVGString(chart)
        assertTrue(svg.isNotEmpty())
    }

    @Test
    fun toPNG() {
        val chart = PriceChart(feed, feed.assets.last())
        val svg = imageCreator.renderToSVGString(chart)

        val arr = transcodeSVG2PNG(svg)
        assertTrue(arr.isNotEmpty())

        val f = File(folder, "test.png")
        transcodeSVG2PNG(svg, f)
        assertTrue(f.exists())
        assertEquals(arr.size.toLong(), Files.size(f.toPath()))
    }


}
