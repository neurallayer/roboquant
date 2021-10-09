package org.roboquant.feeds

import java.time.Instant
import java.util.*

open class HistoricPriceFeed : HistoricFeed {

    protected val events = TreeMap<Instant, MutableList<PriceAction>>()

    override val timeline: List<Instant>
        get() = events.keys.toList()

    override val assets
        get() = events.values.map { priceAction -> priceAction.map { it.asset }.distinct() }.flatten().distinct().toSortedSet()


    /**
     * (Re)play the events of the feed using the provided [EventChannel]
     *
     * @param channel
     * @return
     */
    override suspend fun play(channel: EventChannel) {
        events.forEach {
            val event = Event(it.value, it.key)
            channel.send(event)
        }
    }

}