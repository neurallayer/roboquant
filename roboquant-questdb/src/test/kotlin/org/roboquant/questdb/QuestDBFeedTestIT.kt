package org.roboquant.questdb

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.random.RandomWalkFeed
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals


internal class QuestDBFeedTestIT {

    @TempDir
    lateinit var folder: File

    @Test
    fun basic() {
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

    @Test
    fun priceQuotes() {

        class QuoteFeed : Feed {
            override suspend fun play(channel: EventChannel) {
                val asset = Asset("TEST")
                val now = Instant.now()
                repeat(100) {
                    val action = PriceQuote(asset, 100.0, 10000.0, 100.0, 10000.0)
                    val event = Event(listOf(action), now + it.millis)
                    channel.send(event)
                }
            }

        }

        val recorder = QuestDBRecorder(folder.toPath())
        val feed = QuoteFeed()
        val name = "quotes"

        assertDoesNotThrow {
            recorder.record<PriceQuote>(feed, name)
        }

        val outputFeed = QuestDBFeed(name, folder.toPath())
        assertEquals(1, outputFeed.assets.size)
        println(outputFeed.timeframe)
        assertEquals(99, outputFeed.timeframe.duration.toMillisPart())
        outputFeed.close()
    }

    @Test
    fun tradePrice() {

        class TradeFeed : Feed {
            override suspend fun play(channel: EventChannel) {
                val asset = Asset("TEST")
                val now = Instant.now()
                repeat(100) {
                    val action = TradePrice(asset, 100.0, 10000.0)
                    val event = Event(listOf(action), now + it.millis)
                    channel.send(event)
                }
            }

        }

        val recorder = QuestDBRecorder(folder.toPath())
        val feed = TradeFeed()
        val name = "trades"

        assertDoesNotThrow {
            recorder.record<TradePrice>(feed, name)
        }

        val outputFeed = QuestDBFeed(name, folder.toPath())
        assertEquals(1, outputFeed.assets.size)
        println(outputFeed.timeframe)
        assertEquals(99, outputFeed.timeframe.duration.toMillisPart())
        outputFeed.close()
    }


}
