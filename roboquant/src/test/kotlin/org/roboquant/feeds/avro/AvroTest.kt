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

package org.roboquant.feeds.avro

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.TestData
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.feeds.*
import java.io.File
import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.test.*

@TestMethodOrder(Alphanumeric::class)
class AvroTest {

    private class MyFeed : Feed {

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
        // folder.create()
        // fileName = folder.newFile("test.avro").path
        fileName = File(folder, "test.avro").path
        val feed = TestData.feed
        assets.addAll(feed.assets)
        size = feed.timeline.size
        AvroUtil.record(feed, fileName, compressionLevel = 0)
        assertTrue(File(fileName).isFile)
    }

    @Test
    fun avroStep2() {
        val feed2 = AvroFeed(Path(fileName), useIndex = false)
        assertTrue(feed2.assets.isEmpty())

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
    fun avroStep3() {
        val feed3 = AvroFeed(fileName, useIndex = true)
        assertEquals(size, feed3.timeline.size)
        assertEquals(nAssets, feed3.assets.size)
        assertEquals(assets.toSet(), feed3.assets.toSet())

        runBlocking {
            var cnt = 0
            for (event in play(feed3)) cnt++
            assertEquals(size, cnt)
        }
    }

    @Test
    fun avroStep4() {
        val asset = Asset("DUMMY")
        val p1 = PriceBar.fromValues(asset, listOf(10.0, 10.0, 10.0, 10.0, 1000.0))
        val p2 = TradePrice(asset, 10.0, 1000.0)
        val p3 = PriceQuote(asset, 10.0, 1000.0, 10.0, 1000.0)
        val feed = MyFeed()
        feed.event = Event(listOf(p1, p2, p3), Instant.now())

        assertDoesNotThrow {
            AvroUtil.record(feed, fileName, compressionLevel = 0)
        }

        val feed2 = AvroFeed(fileName)
        assertEquals(1, feed2.timeline.size)

    }

    @Test
    fun predefined() {
        val feed = AvroFeed.sp500()
        assertTrue(feed.assets.size >= 490)
        assertTrue(feed.timeframe.start >= Instant.parse("2016-01-01T00:00:00Z"))
        assertContains(feed.assets.map { it.symbol }, "AAPL")

        val feed2 = AvroFeed.usTest()
        assertTrue(feed2.assets.size == 6)
        assertContains(feed2.assets.map { it.symbol }, "AAPL")
    }

    @Test
    fun loadFromGithub() {
        Config.getProperty("FULL_COVERAGE") ?: return
        val file = (Config.home / "us_small_daily_v2.0.avro").toFile()
        file.delete()
        assertFalse(file.exists())
        val feed = AvroFeed.usTest()
        assertTrue(feed.assets.size == 6)

        val file2 = (Config.home / "us_small_daily_v2.0.avro").toFile()
        assertTrue(file2.exists())
    }

}