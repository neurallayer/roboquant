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
import kotlin.test.*
import org.junit.rules.TemporaryFolder
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.feeds.play
import java.io.File
import java.time.Instant


class AvroTest {

    private val folder = TemporaryFolder()


    @Test
    fun avroRecord() {
        folder.create()
        val fileName = folder.newFile("test.avro").path
        val nAssets = 3
        val feed = RandomWalk.lastYears(1, nAssets = nAssets)
        val size = feed.timeline.size
        AvroUtil.record(feed, fileName)
        assertTrue(File(fileName).isFile)

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

        val feed3 = AvroFeed(fileName, useIndex = true)
        assertEquals(feed.assets, feed3.assets)
        assertEquals(size, feed3.timeline.size)
        assertEquals(size, feed3.index.size)

        runBlocking {
            var cnt = 0
            for (event in play(feed3)) cnt++
            assertEquals(size, cnt)
        }

    }


}