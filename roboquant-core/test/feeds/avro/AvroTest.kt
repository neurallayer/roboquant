/*
 * Copyright 2021 Neural Layer
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
import org.junit.FixMethodOrder
import org.junit.rules.TemporaryFolder
import org.junit.runners.MethodSorters
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.common.days
import org.roboquant.feeds.play
import org.roboquant.feeds.random.RandomWalk
import java.io.File
import java.time.Instant
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AvroTest {

    companion object {
        private val folder = TemporaryFolder()
        private lateinit var fileName: String
        private var size: Int = 0
        private const val nAssets = 3
    }

    @Test
    fun avroStep1() {
        folder.create()
        fileName = folder.newFile("test.avro").path
        val timeline = Timeframe.past(30.days).toDays()
        val feed = RandomWalk(timeline, nAssets = nAssets)
        size = feed.timeline.size
        AvroUtil.record(feed, fileName, compressionLevel = 0)
        assertTrue(File(fileName).isFile)
    }


    @Test
    fun avroStep2() {
        val feed2 = AvroFeed(fileName, useIndex = false)
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
        // assertEquals(feed.assets, feed3.assets)
        assertEquals(size, feed3.timeline.size)
        assertEquals(size, feed3.index.size)
        assertEquals(nAssets, feed3.assets.size)

        runBlocking {
            var cnt = 0
            for (event in play(feed3)) cnt++
            assertEquals(size, cnt)
        }
    }

    @Test
    fun predefined() {
        val feed = AvroFeed.sp500()
        assertTrue(feed.assets.size >= 500)
        assertTrue(feed.timeframe.start > Instant.parse("2013-01-01T00:00:00Z"))
        assertContains(feed.assets.map { it.symbol }, "AAPL")

        val feed2 = AvroFeed.usTest()
        assertTrue(feed2.assets.size == 6)
        assertContains(feed2.assets.map { it.symbol }, "AAPL")
        assertTrue(feed2.timeframe.start < Instant.parse("1963-01-01T00:00:00Z"))
    }

    @Test
    fun loadFromGithub() {
        Config.getProperty("TEST_DATA") ?: return
        val file = (Config.home / "us_stocks_test_v1.1.avro").toFile()
        file.delete()
        val feed = AvroFeed.usTest()
        assertTrue(feed.assets.size == 6)
    }



}