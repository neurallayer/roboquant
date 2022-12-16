/*
 * Copyright 2020-2022 Neural Layer
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
    fun isFilled(asset: Asset) = data[asset]?.isFull() ?: false

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