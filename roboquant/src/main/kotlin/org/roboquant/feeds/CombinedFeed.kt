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

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Timeframe
import java.util.*

/**
 * Combines several feeds into a single new feed. It works both with historic feeds and live feeds. However, it may
 * cause delay in the delivery of events in live feeds. If you don't want this, use [CombinedLiveFeed] instead.
 *
 * @property feeds the feeds to combine in a single new feed
 * @property channelCapacity the channel capacity to use per feed
 * @constructor Create a new Combined Feed
 */
class CombinedFeed(vararg val feeds: Feed, private val channelCapacity: Int = 1) : Feed {

    private data class QueueEntry(val event: Event, val channel: EventChannel) : Comparable<QueueEntry> {

        override fun compareTo(other: QueueEntry) = event.compareTo(other.event)

    }

    init {
        require(channelCapacity >= 1) { "channelCapacity should be >= 1" }
    }

    /**
     * Return the total timeframe of all the feeds combined
     */
    override val timeframe: Timeframe
        get() {
            if (feeds.isEmpty()) return Timeframe.EMPTY
            val tfs = feeds.map { it.timeframe }
            val start = tfs.minBy { it.start }.start
            val last = tfs.maxBy { it.end }
            return Timeframe(start, last.end, last.inclusive)
        }


    private suspend fun process(mainChannel: EventChannel, channels: List<EventChannel>) {
        val queue = PriorityQueue<QueueEntry>(feeds.size)

        // Prefill queue with one entry per channel
        for (channel in channels) {
            try {
                val event = channel.receive()
                queue.add(QueueEntry(event, channel))
            } catch (_: ClosedReceiveChannelException) {
                // NOP
            }
        }

        while (queue.isNotEmpty()) {
            val (event, channel) = queue.remove()
            mainChannel.send(event)

            // Try to fill the queue with a new event from the channel that was just consumed.
            try {
                val newEvent = channel.receive()
                val entry = QueueEntry(newEvent, channel)
                queue.add(entry)
            } catch (_: ClosedReceiveChannelException) {
                // NOP
            }
        }
    }

    /**
     * @see Feed.play
     */
    override suspend fun play(channel: EventChannel) {
        val jobs = ParallelJobs()
        val channels = mutableListOf<EventChannel>()
        for (feed in feeds) {
            val feedChannel = EventChannel(channelCapacity, channel.timeframe)
            channels.add(feedChannel)
            jobs.add(feed.playBackground(feedChannel))
        }

        jobs.add {
            process(channel, channels)
        }

        jobs.joinAll()
    }

    /**
     * Close all underlying [feeds]
     */
    override fun close() {
        feeds.forEach { it.close() }
    }

}
