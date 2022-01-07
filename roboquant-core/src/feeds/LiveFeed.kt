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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * Live feed represents a feed of live data. So data that comes in as it is observed with timestamps
 * close to the present. Since a live feed has no defined end, it will continue to run unless you specify
 * a timeframe.
 *
 * This default implementation generates heartbeat signals to ensure components still have an opportunity
 * to act even if no other data is incoming. A heartbeat is an [Event] without actions.
 *
 */
abstract class LiveFeed : Feed {

    /**
     * How many milliseconds between the heartbeat signals, default is 10_000ms (10 seconds)
     */
    var heartbeatInterval: Long = 10_000
    protected var channel: EventChannel? = null

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        this.channel = channel
        try {
            while (true) {
                delay(heartbeatInterval)
                val event = Event(listOf(), Instant.now())
                channel.send(event)
                if (channel.done) break
            }
        } catch (e: ClosedSendChannelException) {
            // Expected exception
        } finally {
            this.channel = null
        }

    }


}