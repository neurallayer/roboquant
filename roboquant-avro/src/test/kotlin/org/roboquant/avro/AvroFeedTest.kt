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

package org.roboquant.avro

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestMethodOrder(Alphanumeric::class)
internal class AvroFeedTest {

    private class MyFeed(override val assets: Set<Asset>) : AssetFeed {

        val events = mutableListOf<Event>()

        override suspend fun play(channel: EventChannel) {
            for (event in events) channel.send(event)
        }

    }


    private fun play(feed: Feed, timeframe: Timeframe = Timeframe.INFINITE): EventChannel {
        val channel = EventChannel(timeframe = timeframe)
        feed.playBackground(channel)
        return channel
    }


    internal companion object {

        @TempDir
        lateinit var folder: File

        private val fileName: String
            get() = File(folder, "test2.avro").path.toString()

    }




    @Test
    fun feedPlayback() {
        val feed3 = AvroFeed(fileName)
        assertTrue(feed3.timeframe.inclusive)
        val tf = feed3.timeframe
        runBlocking {
            var cnt = 0
            for (event in play(feed3)) {
                assertTrue(event.time in tf)
                cnt++
            }
        }
    }

    @Test
    fun supportedPriceActions() {
        val asset = Stock("DUMMY")
        val p1 = PriceBar(asset, 10.0, 10.0, 10.0, 10.0, 1000.0)
        val p2 = TradePrice(asset, 10.0, 1000.0)
        val p3 = PriceQuote(asset, 10.0, 1000.0, 10.0, 1000.0)
        val p4 = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(100.0, 11.0)),
            listOf(OrderBook.OrderBookEntry(50.0, 9.0))
        )
        val feed = MyFeed(setOf(asset))
        val now = Instant.now()
        feed.events.add(Event(now + 1.millis, listOf(p1)))
        feed.events.add(Event(now + 2.millis, listOf(p2)))
        feed.events.add(Event(now + 3.millis, listOf(p3)))
        feed.events.add(Event(now + 4.millis, listOf(p4)))

        val feed2 = AvroFeed(fileName)
        assertDoesNotThrow {
            feed2.record(feed)
        }

        val actions = feed2.filter<PriceItem>().map { it.second }
        assertEquals(4, actions.size)

    }



    @Test
    fun append() {
        val now = Instant.now()
        val past = Timeframe(now - 2.years, now - 1.years)
        val feed = RandomWalk(past, 1.days)
        val fileName = File(folder, "test2.avro").path
        val feed2 = AvroFeed(fileName)
        feed2.record(feed, compress = true)

        val past2 = Timeframe(now - 1.years, now)
        val feed3 = RandomWalk(past2, 1.days)
        feed2.record(feed3, append = true)
    }

    @Test
    fun assetSerialization() {
        val asset1 = Stock("XYZ")
        val str = asset1.serialize()
        val asset2 = Asset.deserialize(str)
        assertEquals(asset1, asset2)
    }

    @Test
    fun sp25() {
        assertDoesNotThrow {
            val feed = AvroFeed.sp25()
            assertTrue(feed.exists())
            assertEquals(Timeframe.parse("2021-01-04T21:00:00Z", "2024-12-31T21:00:00Z", inclusive = true), feed.timeframe)
        }
    }

}
