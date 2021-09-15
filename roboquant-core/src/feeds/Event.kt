package org.roboquant.feeds

import org.roboquant.common.Asset
import java.time.Instant

/**
 * Event contains a list of actions that all happened at the same moment in time ([now]). There are no restrictions
 * on the type of information contained in a [Action] and different type of actions can be mixed in a single event.
 *
 * @property actions The list of actions
 * @property now The moment in time this event became available
 * @constructor Create new Event
 */
data class Event(val actions: List<Action>, val now: Instant) : Comparable<Event> {

    /**
     * Convenience property for accessing all the price actions in this event. The result is cached so that accessing
     * this attribute many times is very quick.
     *
     * Please note this only supports a single price action per asset. If there are multiple price actions for a single
     * asset, the last one found will be returned.
     */
    val prices: Map<Asset, PriceAction> by lazy {
        actions.filterIsInstance<PriceAction>().associateBy { it.asset }
    }

    /**
     * Convenience method to get a single price for an asset or null if there is no price action present for
     * the asset in this event
     *
     * @param asset
     * @param type
     * @return
     */
    fun getPrice(asset: Asset, type: String = "DEFAULT"): Double? {
        return prices[asset]?.getPrice(type)
    }

    /**
     * Compare 2 events based on their timestamp. This is used for sorting a list of events in their
     * chronological order.
     *
     * @param other
     * @return
     */
    override fun compareTo(other: Event): Int = this.now.compareTo(other.now)

}


/**
 * Merge one list of events into another one and result the result
 *
 * @param feed
 */
fun List<Event>.merge(feed: List<Event>): List<Event> {
    val result = associate { it.now to it.actions }.toMutableMap()
    for ((actions, now) in feed) {
        result[now] = result.getOrDefault(now, listOf()) + actions
    }
    return result.toSortedMap().toList().map { Event(it.second, it.first) }
}


/**
 * Assets
 *
 */
fun List<Event>.assets() = this.asSequence().filterIsInstance<PriceAction>().map { it.asset }.toSet()



