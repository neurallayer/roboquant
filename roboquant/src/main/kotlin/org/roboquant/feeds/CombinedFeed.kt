package org.roboquant.feeds

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.roboquant.common.ParallelJobs
import org.roboquant.common.Timeframe
import java.util.*

/**
 * Combines several feeds into a single new feed. It works both with historic feeds and live feeds, however it may cause
 * delay in the delivery of events in live feeds. If you don't want this, use [CombinedLiveFeed] instead.
 *
 * @property feeds
 * @property channelCapacity the channel capacity to use per feed.
 * @constructor Create empty Relay channel
 */
class CombinedFeed(vararg val feeds: Feed, private val channelCapacity:Int = 1) : Feed {

    private data class QueueEntry(val event: Event, val channel: EventChannel) : Comparable<QueueEntry> {

        override fun compareTo(other: QueueEntry): Int {
            return event.compareTo(other.event)
        }

    }

    /**
     * Return the total timeframe of all feeds combined
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
            jobs.add {
                feed.play(feedChannel)
                feedChannel.close()
            }
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