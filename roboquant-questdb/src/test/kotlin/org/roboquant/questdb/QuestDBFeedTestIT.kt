/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.questdb

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals


internal class QuestDBFeedTestIT {

    @TempDir
    lateinit var folder: File


    @Test
    fun basic() {
        val recorder = QuestDBFeed("pricebars", folder.toPath())
        val inputFeed = RandomWalk.lastYears(1)

        recorder.record<PriceBar>(inputFeed)

        val outputFeed = QuestDBFeed("pricebars", folder.toPath())

        assertEquals(inputFeed.assets, outputFeed.assets)
        assertEquals(inputFeed.timeline.timeframe, outputFeed.timeframe)

        outputFeed.close()
    }

    @Test
    fun append() {
        val recorder = QuestDBFeed("pricebars2", folder.toPath())
        val inputFeed = RandomWalk.lastYears(1)
        val tfs = inputFeed.timeframe.split(3.months)
        recorder.record<PriceBar>(inputFeed, tfs.first())
        tfs.drop(1).forEach { recorder.record<PriceBar>(inputFeed, it, true) }

        val outputFeed = QuestDBFeed("pricebars2", folder.toPath())

        assertEquals(inputFeed.assets, outputFeed.assets)
        assertEquals(inputFeed.timeline.timeframe, outputFeed.timeframe)
        outputFeed.close()
    }

    @Test
    fun merge() {
        val recorder = QuestDBFeed("pricebars3", folder.toPath())
        val tf = Timeframe.parse("2020", "2022")
        val feed1 = RandomWalk(tf, nAssets = 1)
        val feed2 = RandomWalk(tf, nAssets = 1)

        // Need to partition when adding out-of-order price actions
        recorder.record<PriceBar>(feed1, partition = "YEAR")
        recorder.record<PriceBar>(feed2, append = true)

        val outputFeed = QuestDBFeed("pricebars3", folder.toPath())
        assertEquals(feed1.assets + feed2.assets, outputFeed.assets)
        assertEquals(feed1.timeline.timeframe, outputFeed.timeframe)
        outputFeed.close()
    }



    @Test
    fun record() {

        class QuoteFeed : Feed {
            override suspend fun play(channel: EventChannel) {
                val asset = Stock("TEST")
                val now = Instant.now()
                repeat(100) {
                    val action = PriceQuote(asset, 100.0, 10000.0, 100.0, 10000.0)
                    val event = Event(now + it.millis, listOf(action))
                    channel.send(event)
                }
            }

        }

        val name = "quotes"
        val feed = QuestDBFeed(name, folder.toPath())
        val origin = QuoteFeed()

        assertDoesNotThrow {
            feed.record<PriceQuote>(origin)
        }

        assertEquals(1, feed.assets.size)
        println(feed.timeframe)
        assertEquals(99, feed.timeframe.duration.toMillisPart())
        feed.close()
    }

    @Test
    fun tradePrice() {

        class TradeFeed : Feed {
            override suspend fun play(channel: EventChannel) {
                val asset = Stock("TEST")
                val now = Instant.now()
                repeat(100) {
                    val action = TradePrice(asset, 100.0, 10000.0)
                    val event = Event(now + it.millis, listOf(action))
                    channel.send(event)
                }
            }

        }

        val feed = QuestDBFeed("trades", folder.toPath())
        val origin = TradeFeed()

        assertDoesNotThrow {
            feed.record<TradePrice>(origin)
        }

        assertEquals(1, feed.assets.size)
        println(feed.timeframe)
        assertEquals(99, feed.timeframe.duration.toMillisPart())
        feed.close()
    }


}
