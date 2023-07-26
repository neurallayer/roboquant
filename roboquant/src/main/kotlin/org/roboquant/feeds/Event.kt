/*
 * Copyright 2020-2023 Neural Layer
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
import java.time.Instant

/**
 * An event contains a list of [actions] that all happened at the same moment in [time].
 *
 * @property actions the list of actions that are part of this event
 * @property time the time that the actions in this event became available
 */
class Event(val actions: List<Action>, val time: Instant) : Comparable<Event> {

    /**
     * Convenience property for accessing the price actions in this event. The result is cached so that accessing
     * this property multiple times is quick.
     *
     * If there are multiple price actions for a single asset in the event, the last one found will be returned. If
     * you require access to all prices for an asset, iterate over the [actions] directly.
     */
    val prices: Map<Asset, PriceAction> by lazy {
        actions.filterIsInstance<PriceAction>().associateBy { it.asset }
    }

    /**
     * @suppress
     */
    companion object {

        /**
         * Return an event without any [actions] with as default [time] the current system time.
         */
        fun empty(time: Instant = Instant.now()): Event = Event(emptyList(), time)

    }

    /**
     * Convenience method to get a single price for an [asset] or null if there is no price action present for
     * the asset in this event. Optionally you can specify the [type] of price.
     *
     * If there are multiple price actions for a single asset in the event, the last one found will be returned. If
     * you require access to all prices for an asset, access [actions] directly.
     */
    fun getPrice(asset: Asset, type: String = "DEFAULT"): Double? {
        return prices[asset]?.getPrice(type)
    }

    /**
     * Compare this event to an [other] event based on their [time]. This is used for sorting a list of events by
     * their chronological order.
     */
    override fun compareTo(other: Event): Int = time.compareTo(other.time)
}

/**
 * Return the Timeframe matching the list of events. The collection has to be chronologically ordered.
 */
val Collection<Event>.timeframe: Timeframe
    get() = if (isEmpty()) Timeframe.EMPTY else Timeframe(first().time, last().time, inclusive = true)

