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
import org.roboquant.common.Timeframe
import org.roboquant.common.compareTo
import java.util.logging.Logger

/**
 * Wrapper around a [Channel] for communicating the [events][Event] of a [Feed]. It uses asynchronous communication
 * so the producing and receiving parts are decoupled.
 *
 * It has built in support to restrict the events that are being send to a certain [timeframe]. It is gauranteed that
 * no events outside this timeframe are delivered.
 *
 * @param capacity The capacity of the channel in the number of events it can store before blocking the sender
 * @property timeframe Limit the events to this timeframe only
 * @constructor
 *
 */
open class EventChannel(capacity: Int = 100, val timeframe: Timeframe = Timeframe.INFINITE) {

    private val channel = Channel<Event>(capacity)
    private val logger: Logger = Logging.getLogger(EventChannel::class)

    var done: Boolean = false
        private set

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
        if (event.time in timeframe) {
            while (!channel.trySend(event).isSuccess) {
                if (done) return
                val dropped = channel.tryReceive().getOrNull()
                if (dropped !== null)
                    logger.info { "dropped event for time ${dropped.time}" }
            }
        } else {
            if (event.time > timeframe) {
                logger.fine { "Offer ${event.time} after $timeframe, closing channel" }
                close()
            }
        }
    }

    operator fun iterator() = channel.iterator()


    /**
     * Send an event. If the event is before the timeframe linked to this channel it will be
     * ignored. And if the event is after the timeframe, the channel will be closed.
     *
     * @param event
     */
    suspend fun send(event: Event) {
        if (event.time in timeframe) {
            channel.send(event)
        } else {
            if (event.time > timeframe) {
                logger.fine { "Send ${event.time} after $timeframe, closing channel" }
                close()
            }
        }
    }

    /**
     * Receive an event from the channel. Will throw a [ClosedReceiveChannelException] if the channel is already closed.
     */
    suspend fun receive(): Event {
        while (true) {
            val event = channel.receive()
            timeframe.contains(event.time) && return event
            if (event.time > timeframe) {
                logger.fine { "Received ${event.time} after $timeframe, closing channel" }
                close()
                throw ClosedReceiveChannelException("Out of time")
            }
        }

    }

    /**
     * Close this [EventChannel] and mark it as [done]
     *
     */
    fun close() {
        done = true
        channel.close()
    }

}
