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
import org.junit.jupiter.api.Test
import org.roboquant.common.Timeframe
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EventChannelTest {

    @Test
    fun basicSend(): Unit = runBlocking {
        val channel = EventChannel(10)
        val event = Event.empty()
        repeat(5) {
            channel.send(event)
        }
        assertFalse(channel.done)

        channel.close()
        assertTrue(channel.done)

        assertFails {
            channel.send(event)
        }
    }

    @Test
    fun basicOffer() {
        val channel = EventChannel(10)
        val event = Event.empty()
        repeat(11) {
            channel.offer(event)
        }
        assertFalse(channel.done)
        channel.close()
        assertTrue(channel.done)

    }

    @Test
    fun timeframeOffer() {
        // Create channel with timeframe in the past
        val past = Timeframe.fromYears(2015, 2017)
        val channel = EventChannel(timeframe = past)
        val event = Event.empty()
        channel.offer(event)
        assertTrue(channel.done)
        channel.close()
        assertTrue(channel.done)
    }

}