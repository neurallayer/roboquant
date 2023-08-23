package org.roboquant.questdb

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.*
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.random.RandomWalkFeed
import java.io.File
import kotlin.test.assertEquals


internal class QuestDBFeedTest {

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val recorder = QuestDBRecorder(folder.toPath())
        val inputFeed = RandomWalkFeed.lastYears(1)

        recorder.record<PriceBar>(inputFeed, "pricebars")

        val outputFeed = QuestDBFeed("pricebars", folder.toPath())

        assertEquals(inputFeed.assets, outputFeed.assets)
        assertEquals(inputFeed.timeline.timeframe, outputFeed.timeframe)
        outputFeed.close()
    }


    @Test
    fun append() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val recorder = QuestDBRecorder(folder.toPath())
        val inputFeed = RandomWalkFeed.lastYears(1)
        val tfs = inputFeed.timeframe.split(3.months)
        recorder.record<PriceBar>(inputFeed, "pricebars2", tfs.first())
        tfs.drop(1).forEach { recorder.record<PriceBar>(inputFeed, "pricebars2", it, true) }

        val outputFeed = QuestDBFeed("pricebars2", folder.toPath())

        assertEquals(inputFeed.assets, outputFeed.assets)
        assertEquals(inputFeed.timeline.timeframe, outputFeed.timeframe)
        outputFeed.close()
    }

    @Test
    fun merge() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val recorder = QuestDBRecorder(folder.toPath())
        val tf = Timeframe.parse("2020", "2022")
        val feed1 = RandomWalkFeed(tf, nAssets = 1, template = Asset("ABC"))
        val feed2 = RandomWalkFeed(tf, nAssets = 1, template = Asset("XYZ"))

        // Need to partition when adding out-of-order price actions
        recorder.record<PriceBar>(feed1, "pricebars3", partition = "YEAR")
        recorder.record<PriceBar>(feed2, "pricebars3", append = true)

        val outputFeed = QuestDBFeed("pricebars3", folder.toPath())
        assertEquals(feed1.assets + feed2.assets, outputFeed.assets)
        assertEquals(feed1.timeline.timeframe, outputFeed.timeframe)
        outputFeed.close()
    }


}