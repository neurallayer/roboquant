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

package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Logging
import java.util.*

/**
 * Live feed represents a feed of live data. So data that comes in as it is observed with timestamps
 * close to the present. Since a live feed has no pre-defined end, it will continue to run unless you specify
 * a timeframe during the run.
 *
 * This default implementation generates heartbeat signals to ensure components still have an opportunity
 * to act even if no other data is incoming. A heartbeat is an [empty event][Event.empty].
 *
 * Changing this value during the play of a feed will not impact the interval of the first coming heartbeat.
 */
abstract class LiveFeed : Feed {

    private val logger = Logging.getLogger(this::class)
    private var channels = Collections.synchronizedList(mutableListOf<EventChannel>())


    /**
     * Return true if this live feed is being used in one or more runs, false otherwise
     */
    val isActive: Boolean
        get() = channels.isNotEmpty()

    /**
     * Subclasses should call this method or [sendAsync] to send an event. If no channel is active, any event
     * sent will be dropped.
     */
    protected fun send(event: Event): Unit = runBlocking {
        sendAsync(event)
    }

    /**
     * Subclasses should call this method or [send] to send an event. If no channel is active, any
     * event sent will be dropped.
     */
    protected suspend fun sendAsync(event: Event) {
        for (channel in channels) {
            try {
                channel.send(event)
            } catch (_: ClosedSendChannelException) {
                logger.trace { "closed channel" }
            }
        }
        synchronized(this) {
            channels.removeAll { it.closed }
        }

    }

    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     */
    override suspend fun play(channel: EventChannel) {
        synchronized(this) {
            channels.add(channel)
        }
        channel.waitOnClose()
    }


}
