package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar

/**
 * Multi asset price bar series that keeps a number of history price-bars in memory per asset. All the rolling windows
 * have the same [windowSize].
 *
 * @property windowSize The number of historic price-bars per asset to keep track of
 * @constructor Create a new instance of a MultiAssetPriceBarSeries
 */
class MultiAssetPriceBarSeries(private val windowSize: Int) {

    private val data = mutableMapOf<Asset, PriceBarSeries>()

    /**
     * Add a new priceBar and return true if already filled, false otherwise
     */
    fun add(priceBar: PriceBar): Boolean {
        val series = data.getOrPut(priceBar.asset) { PriceBarSeries(windowSize) }
        return series.add(priceBar)
    }


    /**
     * Add all actions of the type [PriceBar] found in the [event] to this series
     */
    fun add(event: Event) {
        val priceBars = event.actions.filterIsInstance<PriceBar>()
        priceBars.forEach { add(it) }
    }

    /**
     * Is there enough data captured for the provided [asset]
     */
    fun isFilled(asset: Asset) = data[asset]?.isFilled() ?: false

    /**
     * Get the price bar series for the provided [asset]
     *
     * @param asset
     */
    fun getSeries(asset: Asset): PriceBarSeries = data.getValue(asset)

    /**
     * Clear all captured prices for all the assets
     */
    fun clear() = data.clear()

}