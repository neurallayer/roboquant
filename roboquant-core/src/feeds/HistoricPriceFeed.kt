package org.roboquant.feeds

import java.time.Instant
import java.util.*

/**
 * Base class that provides a foundation for data feeds that provide historic prices. It used a TreeMap to store
 * for each event one or more PriceActions.
 */
open class HistoricPriceFeed : HistoricFeed {

    private val events = TreeMap<Instant, MutableList<PriceAction>>()

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


    protected fun add(time: Instant, action: PriceAction) {
        val l = events.getOrPut(time) { mutableListOf()}
        l.add(action)
    }

    /**
     * Clear all events that are currently available
     */
    fun clear() {
        events.clear()
    }

}