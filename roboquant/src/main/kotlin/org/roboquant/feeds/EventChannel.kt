/*
 * Copyright 2020-2025 Neural Layer
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

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelIterator
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import org.roboquant.common.Event
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.compareTo
import java.time.Instant

/**
 * Wrapper around a [Channel] for communicating the [events][org.roboquant.common.Event] of a [Feed].
 * It uses asynchronous communication so the producing and consuming parts are decoupled.
 * An EventChannel has limited capacity to prevent memory problems when using large data feeds.
 *
 * It has built in support to limit the events that are being sent to a certain [timeframe]. It is guaranteed that
 * no events outside that timeframe can be delivered to the channel.
 *
 * @property timeframe Limit the events to this timeframe, default is INFINITE, so no limit
 * @property capacity The capacity of the channel in the number of events it can store before blocking the sender
 * @property onBufferOverflow define behaviour when buffer is full, default is [BufferOverflow.SUSPEND]
 * @constructor create a new EventChannel
 */
class EventChannel(
    val timeframe: Timeframe = Timeframe.INFINITE,
    val capacity: Int = 10,
    private val onBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND
) : AutoCloseable, Cloneable {

    private val channel = Channel<Event>(capacity, onBufferOverflow)
    private val logger = Logging.getLogger(EventChannel::class)
    private val mutex = Mutex(true)

    /**
     * True if the event-channel is closed, false otherwise. A closed event-channel cannot be re-opened again and will
     * throw exceptions if trying to write to it.
     */
    var closed: Boolean = false
        private set

    /**
     * Iterate over the events in this channel
     */
    operator fun iterator(): ChannelIterator<Event> = channel.iterator()

    /**
     * Send an [event] on this channel. If the time of event is before the timeframe of this channel, it will be
     * silently ignored. And if the event is after the timeframe, the channel will be [closed].
     */
    suspend fun send(event: Event) {
        if (event.time in timeframe) {
            channel.send(event)
        } else {
            if (event.time > timeframe) {
                logger.debug { "send time=${event.time} timeframe=$timeframe, closing channel" }
                close()
            }
        }
    }

    /**
     * Send an [event] on this channel if it is not an empty event. If the time of event is before the timeframe
     * if this channel, it will be silently ignored. And if the event is after the timeframe, the channel
     * will be [closed].
     */
    suspend fun sendNotEmpty(event: Event) {
        if (event.isNotEmpty()) send(event)
    }

    /**
     * Receive an event from the channel with an optional timeout. If the timeout occurs within or before the timeframe,
     * a heartbeat (empty event) will be send.
     *
     * This will throw a [ClosedReceiveChannelException] if the channel is [closed].
     *
     * @see Channel.receive
     */
    suspend fun receive(timeOutMillis: Long = -1): Event {
        if (timeOutMillis <= 0) return channel.receive()

        try {
            return withTimeout(timeOutMillis) {
                channel.receive()
            }
        } catch (err: TimeoutCancellationException) {
            logger.debug("timeout occured", err)
            val now = Instant.now()
            if (now > timeframe) {
                close()
                throw ClosedReceiveChannelException("channel closed")
            }
            return Event.empty(now)
        }
    }


    /**
     * Close this [EventChannel] and mark it as [closed]
     */
    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        channel.close()
        mutex.unlock()
    }

    /**
     * Wait for the channel to be closed.
     * If the channel is already closed when invoking this method, it will return immediately.
     */
    suspend fun waitOnClose() {
        if (closed) return
        mutex.lock()
    }

    /**
     * Make a copy. Events on the channel will not be copied, and the new channel will be open by default.
     */
    public override fun clone(): EventChannel {
        return EventChannel(timeframe, capacity, onBufferOverflow)
    }


}
