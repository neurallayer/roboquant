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

import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.common.Timeline
import java.time.Instant
import java.util.SortedSet

/**
 * Base class that provides a foundation for data feeds that provide historic prices. It uses a sorted map to store
 * for each time one or more PriceActions in memory.
 */
open class HistoricPriceFeed : HistoricFeed {

    private val events = sortedMapOf<Instant, MutableList<PriceAction>>()

    override val timeline: Timeline
        get() = events.keys.toList()

    override val timeframe
        get() = if (events.isEmpty()) Timeframe.INFINITE else Timeframe.inclusive(events.firstKey(), events.lastKey())

    override val assets: SortedSet<Asset>
        get() = events.asSequence().map { entry -> entry.value.map { it.asset } }.flatten().toSortedSet()

    /**
     * Return the first event in this feed
     */
    fun first(): Event = Event(events.getValue(events.firstKey()), events.firstKey())

    /**
     * Return the last event in this feed
     */
    fun last(): Event = Event(events.getValue(events.lastKey()), events.lastKey())

    /**
     * Remove all events from this feed, releasing claimed memory.
     */
    override fun close() {
        events.clear()
    }

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

    /**
     * Add a new [action] to this feed at the provided [time]
     */
    @Synchronized
    protected fun add(time: Instant, action: PriceAction) {
        val actions = events.getOrPut(time) { mutableListOf() }
        actions.add(action)
    }

    /**
     * Add all new [actions] to this feed at the provided [time]
     */
    @Synchronized
    protected fun addAll(time: Instant, actions: Collection<PriceAction>) {
        val existing = events.getOrPut(time) { mutableListOf() }
        existing.addAll(actions)
    }

    /**
     * Merge the events in another historic [feed] into this feed.
     */
    fun merge(feed: HistoricPriceFeed) {
        for (event in feed.events) addAll(event.key, event.value)
    }

}