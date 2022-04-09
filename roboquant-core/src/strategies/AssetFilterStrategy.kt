package org.roboquant.strategies

import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction

/**
 * Only allow price actions that meet the provided [filter] to be passed to the underlying [strategy]
 *
 * @property strategy
 * @property filter the filter to
 * @constructor Create new Asset filter strategy
 */
class AssetFilterStrategy(val strategy: Strategy, val filter: AssetFilter) : Strategy by strategy {

    override fun generate(event: Event): List<Signal> {
        val actions = event.actions.filterIsInstance<PriceAction>().filter { filter.filter(it.asset) }
        val newEvent = Event(actions, event.time)
        return strategy.generate(newEvent)
    }

}

/**
 * Convenience extension method
 *
 * @param assetFilter
 */
fun Strategy.filter(assetFilter: AssetFilter) = AssetFilterStrategy(this, assetFilter)