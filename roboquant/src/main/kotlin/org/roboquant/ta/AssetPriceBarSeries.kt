/*
 * Copyright 2020-2026 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.Event
import org.roboquant.common.PriceBar
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
     * Add all the [PriceBar] items found in the [event] to this AssetPriceBarSeries
     */
    fun addAll(event: Event) {
        for (action in event.items.filterIsInstance<PriceBar>()) {
            val series = map.getOrPut(action.asset) { PriceBarSeries(capacity) }
            series.add(action, event.time)
        }
    }

}
