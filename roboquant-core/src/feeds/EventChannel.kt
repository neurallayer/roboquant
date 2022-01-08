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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import java.util.logging.Logger

/**
 * Wrapper around a [Channel] for communicating an [Event] of a [Feed]. It uses asynchronous communication
 * so the producing and receiving parts are decoupled. It has built in support to restrict the events
 * that are being send to a predefined [TimeFrame].
 *
 * @param capacity The capacity of the channel in the number of events it can store before blocking the sender
 * @property timeFrame Limit the events to this timeframe only
 * @constructor
 *
 */
open class EventChannel(capacity: Int = 100, val timeFrame: TimeFrame = TimeFrame.INFINITY) {

    private val channel = Channel<Event>(capacity)
    private val logger: Logger = Logging.getLogger(EventChannel::class)

    var done: Boolean = false

    /**
     * Add a new event to the channel. If the channel is full, it will remove older event first to make room, before
     * adding the new event. So this is a non-blocking send.
     *
     * This method is often preferable over the regular [send] in live trading scenario's since it prioritize more
     * actual data over a large backlog.
     *
     * @param event
     */
    fun offer(event: Event) {
        if (timeFrame.contains(event.time)) {
            while (!channel.trySend(event).isSuccess) {
                val dropped = channel.tryReceive().getOrNull()
                if (dropped !== null)
                    logger.info { "dropped event for time ${dropped.time}" }
            }
        } else {
            if (event.time >= timeFrame.end) {
                logger.fine { "Offer ${event.time} after $timeFrame, closing channel" }
                channel.close()
                done = true
                // throw ClosedSendChannelException("Out of time")
            }
        }
    }

    operator fun iterator() = channel.iterator()


    /**
     * Send an event. If the event is before the time frame linked to this channel it will be
     * ignored. And if the event is after the time frame, the channel will be closed.
     *
     * @param event
     */
    suspend fun send(event: Event) {
        if (timeFrame.contains(event.time)) {
            channel.send(event)
        } else {
            if (event.time >= timeFrame.end) {
                logger.fine { "Send ${event.time} after $timeFrame, closing channel" }
                channel.close()
                done = true
                // throw ClosedSendChannelException("Out of time")
            }
        }
    }

    suspend fun receive(): Event {
        while (true) {
            val event = channel.receive()
            timeFrame.contains(event.time) && return event
            if (event.time >= timeFrame.end) {
                logger.fine { "Received ${event.time} after $timeFrame, closing channel" }
                channel.close()
                done = true
                throw ClosedReceiveChannelException("Out of time")
            }
        }

    }

    fun close() = channel.close()

}