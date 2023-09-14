/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.feeds.util

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.test.*
import org.roboquant.TestData
import org.roboquant.common.Background
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction

fun play(feed: Feed, timeframe: Timeframe = Timeframe.INFINITE): EventChannel {
    val channel = EventChannel(timeframe = timeframe)

    Background.job {
        feed.play(channel)
        channel.close()
    }
    return channel
}

internal class TestFeedTest {

    @Test
    fun testTestFeed() = runBlocking {
        val feed = LiveTestFeed(5..9, delayInMillis = 1)
        var cnt = 0
        for (step in play(feed)) {
            cnt++
        }
        assertEquals(5, cnt)
    }

    @Test
    fun testTestFeedWithItems() = runBlocking {
        val feed = LiveTestFeed(120..130, 130 downTo 120, asset = TestData.euStock(), delayInMillis = 1)
        var cnt = 0
        for (step in play(feed)) {
            cnt++
            assertTrue(step.actions.first() is PriceAction)
        }
        assertEquals(22, cnt)
    }

    @Test
    fun historicFeed() = runBlocking {
        val feed = HistoricTestFeed(120..130, 130 downTo 120, asset = TestData.euStock())
        var cnt = 0
        for (step in play(feed)) {
            cnt++
            assertTrue(step.actions.first() is PriceAction)
        }
        assertEquals(22, cnt)
    }

}
