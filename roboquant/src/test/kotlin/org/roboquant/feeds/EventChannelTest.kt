/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.feeds

import kotlinx.coroutines.runBlocking
import org.roboquant.common.Event
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class EventChannelTest {

    @Test
    fun basicSend(): Unit = runBlocking {
        val channel = EventChannel()
        val event = Event.empty()
        repeat(5) {
            channel.send(event)
        }
        assertFalse(channel.closed)

        channel.close()
        assertTrue(channel.closed)

        assertFails {
            channel.send(event)
        }
    }


}
