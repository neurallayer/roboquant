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

package org.roboquant.feeds

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import org.roboquant.backtest.mutableSynchronisedListOf
import org.roboquant.common.Logging

/**
 * Live feed represents a feed of live data. So data that comes in as it is observed with timestamps
 * close to the present. Since a live feed has no pre-defined end, it will continue to run unless you specify
 * a timeframe during the run.
 *
 * This default implementation generates heartbeat signals to ensure components still have an opportunity
 * to act even if no other data is incoming. A heartbeat is an [empty event][Event.empty].
 *
 * @param heartbeatInterval The interval between heartbeats in milliseconds, default is 10_000 (10 seconds).
 * Changing this value during the play of a feed will not impact the interval of the first coming heartbeat.
 */
abstract class LiveFeed(var heartbeatInterval: Long = 10_000) : Feed {

    private val logger = Logging.getLogger(this::class)
    private var channels = mutableSynchronisedListOf<EventChannel>()

    init {
        playAll()
    }

    /**
     * Return true if this live feed is being used in one or more runs, false otherwise
     */
    val isActive: Boolean
        get() = channels.isNotEmpty()

    /**
     * Subclasses should use this method to send an event. If no channel is active, any event sent will be dropped and
     * false will be returned.
     */
    @Synchronized
    protected fun send(event: Event) {
        for (channel in channels) {
            try {
                channel.trySend(event)
            } catch (_: ClosedSendChannelException) {
                logger.trace { "closed channel" }
            }
        }
        channels.removeAll { it.closed }
    }


    private fun playAll() {

        CoroutineScope(Dispatchers.Default + Job()).launch {
            while (true) {
                delay(heartbeatInterval)
                val event = Event.empty()
                send(event)
            }
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