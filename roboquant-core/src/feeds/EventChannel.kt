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
open class EventChannel(capacity: Int = 100, val timeFrame: TimeFrame = TimeFrame.FULL) {

    private val channel = Channel<Event>(capacity)
    private val logger: Logger = Logging.getLogger("EventChannel")

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
        if (timeFrame.contains(event.now)) {
            while (!channel.trySend(event).isSuccess) {
                val dropped = channel.tryReceive().getOrNull()
                if (dropped !== null)
                    logger.info { "dropped event for time ${dropped.now}" }
            }
        } else {
            if (event.now >= timeFrame.end) {
                logger.fine { "Offer ${event.now} after $timeFrame, closing channel" }
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
        if (timeFrame.contains(event.now)) {
            channel.send(event)
        } else {
            if (event.now >= timeFrame.end) {
                logger.fine { "Send ${event.now} after $timeFrame, closing channel" }
                channel.close()
                done = true
                // throw ClosedSendChannelException("Out of time")
            }
        }
    }

    suspend fun receive(): Event {
        while (true) {
            val event = channel.receive()
            timeFrame.contains(event.now) && return event
            if (event.now >= timeFrame.end) {
                logger.fine { "Received ${event.now} after $timeFrame, closing channel" }
                channel.close()
                done = true
                throw ClosedReceiveChannelException("Out of time")
            }
        }

    }

    fun close() = channel.close()

}