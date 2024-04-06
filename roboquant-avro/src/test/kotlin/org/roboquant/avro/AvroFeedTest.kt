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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.roboquant.TestData
import org.roboquant.common.*
import org.roboquant.feeds.*
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.feeds.util.AssetSerializer.deserialize
import org.roboquant.feeds.util.AssetSerializer.serialize
import org.roboquant.feeds.util.HistoricTestFeed
import java.io.File
import java.time.Instant
import java.util.*
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestMethodOrder(Alphanumeric::class)
internal class AvroFeedTest {

    private class MyFeed(override val assets: SortedSet<Asset>) : AssetFeed {

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

        private lateinit var fileName: String
        private var size: Int = 0
        private const val NR_ASSETS = 2
        var assets = mutableListOf<Asset>()
    }

    @Test
    fun avroStep1() {
        fileName = File(folder, "test.avro").path
        val feed = TestData.feed
        assets.addAll(feed.assets)
        size = feed.toList().size
        AvroFeed.record(feed, fileName)
        assertTrue(File(fileName).isFile)
    }

    @Test
    fun avroStep2() {
        val feed2 = AvroFeed(Path(fileName))
        runBlocking {
            var past = Instant.MIN
            var cnt = 0
            for (event in play(feed2)) {
                assertTrue(event.time > past)
                assertEquals(NR_ASSETS, event.items.size)
                past = event.time
                cnt++
            }
            assertEquals(size, cnt)
        }
    }

    @Test
    fun cache() {
        val fileName = File(folder, "test2.avro").path
        val feed = TestData.feed
        assets.addAll(feed.assets)
        size = feed.toList().size
        AvroFeed.record(feed, fileName)
        assertTrue(File(fileName).isFile)


        AvroFeed(Path(fileName), useCache = true)
        val file = File(fileName + MetadataProvider.CACHE_SUFFIX)
        assertTrue(file.isFile)
        val index = MetadataProvider(Path(fileName))
        index.clearCache()
        assertFalse(file.exists())

    }

    @Test
    fun feedPlayback() {
        val feed3 = AvroFeed(fileName)
        assertEquals(NR_ASSETS, feed3.assets.size)
        assertEquals(assets.toSet(), feed3.assets.toSet())
        assertTrue(feed3.timeframe.inclusive)

        runBlocking {
            var cnt = 0
            for (event in play(feed3)) cnt++
            assertEquals(size, cnt)
        }
    }

    @Test
    fun supportedPriceActions() {
        val asset = Asset("DUMMY")
        val p1 = PriceBar(asset, 10.0, 10.0, 10.0, 10.0, 1000.0)
        val p2 = TradePrice(asset, 10.0, 1000.0)
        val p3 = PriceQuote(asset, 10.0, 1000.0, 10.0, 1000.0)
        val p4 = OrderBook(
            asset,
            listOf(OrderBook.OrderBookEntry(100.0, 11.0)),
            listOf(OrderBook.OrderBookEntry(50.0, 9.0))
        )
        val feed = MyFeed(sortedSetOf(asset))
        val now = Instant.now()
        feed.events.add(Event(now + 1.millis, listOf(p1)))
        feed.events.add(Event(now + 2.millis, listOf(p2)))
        feed.events.add(Event(now + 3.millis, listOf(p3)))
        feed.events.add(Event(now + 4.millis, listOf(p4)))

        assertDoesNotThrow {
            AvroFeed.record(feed, fileName)
        }

        val feed2 = AvroFeed(fileName)
        val actions = feed2.filter<PriceItem>().map { it.second }
        assertEquals(4, actions.size)

    }

    @Test
    fun unsupportedPriceAction() {

        class MyPrice(override val asset: Asset, override val volume: Double) : PriceItem {
            override fun getPrice(type: PriceType): Double {
                return 10.0
            }
        }

        val asset = Asset("DUMMY")
        val p1 = MyPrice(asset, 100.0)
        val feed = MyFeed(sortedSetOf(asset))
        feed.events.add(Event(Instant.now(), listOf(p1)))

        assertThrows<UnsupportedException> {
            AvroFeed.record(feed, fileName)
        }
    }

    @Test
    fun append() {
        val now = Instant.now()
        val past = Timeframe(now - 2.years, now - 1.years)
        val feed = RandomWalkFeed(past, 1.days)
        val fileName = File(folder, "test2.avro").path

        AvroFeed.record(feed, fileName, compression = true)
        var avroFeed = AvroFeed(fileName)
        assertEquals(feed.assets, avroFeed.assets)

        val past2 = Timeframe(now - 1.years, now)
        val feed2 = RandomWalkFeed(past2, 1.days)
        AvroFeed.record(feed2, fileName, append = true)
        avroFeed = AvroFeed(fileName)
        assertEquals(feed.assets + feed2.assets, avroFeed.assets)
    }

    @Test
    fun timeSpan() {
        val feed = HistoricTestFeed(priceBar = true)
        val pb = feed.toList().first().items.first()
        assertTrue(pb is PriceBar)
        assertEquals(1.days, pb.timeSpan)

        val fileName = File(folder, "test_timespan.avro").path
        AvroFeed.record(feed, fileName, compression = true)

        val avroFeed = AvroFeed(fileName)
        val pb2 = avroFeed.toList().first().items.first()
        assertTrue(pb2 is PriceBar)
        assertEquals(1.days, pb2.timeSpan)
    }

    @Test
    fun assetSerialization() {
        val asset1 = Asset("XYZ")
        val str = asset1.serialize()
        assertEquals("XYZ", str)
        val asset2 = str.deserialize()
        assertEquals(asset1, asset2)

        val asset3 =
            Asset("XYZ", AssetType.BOND, currencyCode = "EUR", exchangeCode = "AEB", multiplier = 2.0, id = "123")
        val str3 = asset3.serialize()
        assertEquals("XYZ\u001FBOND\u001FEUR\u001FAEB\u001F2.0\u001F123", str3)
        val asset4 = str3.deserialize()
        assertEquals(asset3, asset4)

    }

}
