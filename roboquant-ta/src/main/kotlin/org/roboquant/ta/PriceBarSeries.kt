package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar

/**
 * Contains the [PriceBarSerie] data for a collection of Assets
 *
 * @property capacity
 */
class PriceBarSeries private constructor(
    private val capacity: Int,
    private val map: MutableMap<Asset, PriceBarSerie>
) : MutableMap<Asset, PriceBarSerie> by map {

    /**
     * Create a new PriceBarSeries with the provided [capacity] per asset.
     */
    constructor(capacity: Int) : this(capacity, mutableMapOf())

    /**
     * Add a single [priceBar]
     */
    fun add(priceBar: PriceBar): Boolean {
        val priceBuffer = map.getOrPut(priceBar.asset) { PriceBarSerie(capacity) }
        priceBuffer.add(priceBar)
        return priceBuffer.isFull()
    }

    /**
     * Add all the [PriceBar] actions found in the [event] to this PriceBarSeries
     */
    fun addAll(event: Event) {
        for (action in event.actions.filterIsInstance<PriceBar>()) {
            val priceBuffer = map.getOrPut(action.asset) { PriceBarSerie(capacity) }
            priceBuffer.add(action)
        }
    }

}