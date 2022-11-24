/*
 * Copyright 2020-2022 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.roboquant.TestData
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.io.File
import java.time.Instant
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.test.*

@TestMethodOrder(Alphanumeric::class)
class AvroFeedTest {

    private class MyFeed(override val assets: SortedSet<Asset>) : AssetFeed {

        lateinit var event: Event

        override suspend fun play(channel: EventChannel) {
            channel.send(event)
        }

    }

    internal companion object {

        @TempDir
        lateinit var folder: File

        // private val folder = TemporaryFolder()
        private lateinit var fileName: String
        private var size: Int = 0
        private const val nAssets = 2
        var assets = mutableListOf<Asset>()
    }

    @Test
    fun avroStep1() {
        fileName = File(folder, "test.avro").path
        val feed = TestData.feed
        assets.addAll(feed.assets)
        size = feed.timeline.size
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
                assertEquals(nAssets, event.actions.size)
                past = event.time
                cnt++
            }
            assertEquals(size, cnt)
        }
    }

    @Test
    fun feedPlayback() {
        val feed3 = AvroFeed(fileName)
        assertEquals(nAssets, feed3.assets.size)
        assertEquals(assets.toSet(), feed3.assets.toSet())

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
        feed.event = Event(listOf(p1, p2, p3, p4), Instant.now())

        assertDoesNotThrow {
            AvroFeed.record(feed, fileName)
        }

        val feed2 = AvroFeed(fileName)
        val actions = feed2.filter<PriceAction>().map { it.second }
        assertEquals(4, actions.size)

    }

    @Test
    fun unsupportedPriceAction() {

        class MyPrice(override val asset: Asset, override val volume: Double) : PriceAction {
            override fun getPrice(type: String): Double {
                return 10.0
            }
        }

        val asset = Asset("DUMMY")
        val p1 = MyPrice(asset, 100.0)
        val feed = MyFeed(sortedSetOf(asset))
        feed.event = Event(listOf(p1), Instant.now())

        assertThrows<UnsupportedException> {
            AvroFeed.record(feed, fileName)
        }
    }

    @Test
    fun append() {
        val now = Instant.now()
        val past = Timeframe(now - 2.years, now - 1.years).toTimeline(1.days)
        val feed = RandomWalkFeed(past)
        val fileName = File(folder, "test2.avro").path

        AvroFeed.record(feed, fileName, compression = true)
        var avroFeed =AvroFeed(fileName)
        assertEquals(feed.assets, avroFeed.assets)

        val past2 = Timeframe(now - 1.years, now).toTimeline(1.days)
        val feed2 = RandomWalkFeed(past2)
        AvroFeed.record(feed2, fileName, append = true)
        avroFeed = AvroFeed(fileName)
        assertEquals(feed.assets + feed2.assets,  avroFeed.assets)
    }

    @Test
    fun predefined() {
        val feed = AvroFeed.sp500()
        assertTrue(feed.assets.size >= 490)
        assertTrue(feed.timeframe.start >= Instant.parse("2016-01-01T00:00:00Z"))
        assertContains(feed.assets.symbols, "AAPL")

        val feed2 = AvroFeed.sp500Quotes()
        assertTrue(feed2.assets.size >= 490)
        assertContains(feed2.assets.symbols, "AAPL")
    }

    @Test
    fun loadFromGithub() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val fileName = "sp500_pricebar_v5.0.avro"
        val file = (Config.home / fileName).toFile()
        file.delete()
        assertFalse(file.exists())

        // Force loading of file
        AvroFeed.sp500()
        val file2 = (Config.home / fileName).toFile()
        assertTrue(file2.exists())
    }

}