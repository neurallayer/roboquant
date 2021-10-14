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