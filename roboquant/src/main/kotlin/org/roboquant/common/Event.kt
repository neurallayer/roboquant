package org.roboquant.common

import java.time.Instant

/**
 * An event contains a list of [items] that all happened at the same moment in [time]. An [Item]
 * can be anything, but a common use case is price items like candlesticks.
 *
 * @property items the list of actions that are part of this event
 * @property time the time that the actions in this event became available
 */
class Event(val time: Instant, val items: List<Item>) : Comparable<Event> {

    /**
     * Convenience property for accessing the price actions in this event. The result is cached so that accessing
     * this property multiple times is quick.
     *
     * If there are multiple price actions for a single asset in the event, the last one found will be returned. If
     * you need access to all prices for an asset, iterate over the [items] directly.
     */
    val prices: Map<Asset, PriceItem> by lazy {
        buildMap(items.size) {
            for (action in items) {
                if (action is PriceItem) {
                    set(action.asset, action)
                }
            }
        }
    }

    /**
     * @suppress
     */
    companion object {

        /**
         * Return an event without any [items] with as default [time] the current system time.
         */
        fun empty(time: Instant = Instant.now()): Event = Event(time, emptyList())

    }

    /**
     * Convenience method to get a single price for an [asset] or null if there is no price action present for
     * the asset in this event. Optionally you can specify the [type] of price.
     *
     * If there are multiple price actions for a single asset in the event, the last one found will be returned. If
     * you require access to all prices for an asset, access [items] directly.
     */
    fun getPrice(asset: Asset, type: String = "DEFAULT"): Double? {
        return prices[asset]?.getPrice(type)
    }

    /**
     * Compare this event to an [other] event based on their [time]. This is used for sorting a list of events by
     * their chronological order.
     */
    override fun compareTo(other: Event): Int = time.compareTo(other.time)

    /**
     * Return true if this is event has at least one action, false otherwise
     */
    fun isNotEmpty(): Boolean = items.isNotEmpty()

    /**
     * Return true if this event has no actions, false otherwise
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Provide the event time
     */
    override fun toString(): String {
        return "Event(time=$time actions=${items.size})"
    }
}
