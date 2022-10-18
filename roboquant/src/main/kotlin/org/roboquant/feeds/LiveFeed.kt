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

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay

/**
 * Live feed represents a feed of live data. So data that comes in as it is observed with timestamps
 * close to the present. Since a live feed has no pre-defined end, it will continue to run unless you specify
 * a timeframe.
 *
 * This default implementation generates heartbeat signals to ensure components still have an opportunity
 * to act even if no other data is incoming. A heartbeat is an [empty event ][Event.empty].
 *
 * @param heartbeatInterval The interval between heartbeats in milliseconds, default is 10_000 (10 seconds)
 */
abstract class LiveFeed(var heartbeatInterval: Long = 10_000) : Feed {

    private var channel: EventChannel? = null

    /**
     * Subclasses should use this method to send an event. If the channel is not active , it will be dropped.
     */
    protected fun send(event: Event) = channel?.offer(event)

    /**
     * Returns true if this feed is currently active, false otherwise
     */
    val isActive
        get() = channel != null

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     */
    override suspend fun play(channel: EventChannel) {
        this.channel = channel
        try {
            while (true) {
                delay(heartbeatInterval)
                val event = Event.empty()
                channel.send(event)

                // Check so we don't wait for another heart beat
                if (channel.done) break
            }
        } catch (_: ClosedSendChannelException) {
            // Expected exception
        } finally {
            this.channel = null
        }

    }

}