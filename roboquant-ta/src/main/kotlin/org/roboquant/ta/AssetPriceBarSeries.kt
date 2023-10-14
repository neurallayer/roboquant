package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Subclass of a `MutableMap<Asset, PriceBarSeries>` that makes it convenient to track price-bar-series for
 * a group of assets.
 * For each asset, the price-bar-series is tracked individually.
 */
class AssetPriceBarSeries private constructor(
    private val capacity: Int,
    private val map: MutableMap<Asset, PriceBarSeries>
) : MutableMap<Asset, PriceBarSeries> by map {

    /**
     * Create a new AssetPriceBarSeries with the provided [capacity] per asset.
     *
     * @param capacity the max history to track for each asset
     */
    constructor(capacity: Int) : this(capacity, mutableMapOf())

    /**
     * Add a single [priceBar] to this instance and return true of the series for that asset is full.
     */
    fun add(priceBar: PriceBar, time: Instant = Instant.MIN): Boolean {
        val series = map.getOrPut(priceBar.asset) { PriceBarSeries(capacity) }
        series.add(priceBar, time)
        return series.isFull()
    }

    /**
     * Add all the [PriceBar] actions found in the [event] to this AssetPriceBarSeries
     */
    fun addAll(event: Event) {
        for (action in event.actions.filterIsInstance<PriceBar>()) {
            val series = map.getOrPut(action.asset) { PriceBarSeries(capacity) }
            series.add(action, event.time)
        }
    }

}
