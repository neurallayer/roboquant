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

package org.roboquant.strategies.utils

import org.roboquant.common.Asset
import org.roboquant.common.div
import org.roboquant.common.plus
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar

/**
 * PriceBarSeries is a moving window of OHLCV values.
 *
 * @param windowSize the size of the moving window
 *
 * @constructor Create new instance of PriceBarSeries
 */
class PriceBarSeries(windowSize: Int) {

    // The individual moving windows

    private val openSeries = PriceSeries(windowSize)
    private val highSeries = PriceSeries(windowSize)
    private val lowSeries = PriceSeries(windowSize)
    private val closeSeries = PriceSeries(windowSize)
    private val volumeSeries = PriceSeries(windowSize)

    /**
     * Open prices
     */
    val open
        get() = openSeries.toDoubleArray()

    /**
     * High prices
     */
    val high
        get() = highSeries.toDoubleArray()

    /**
     * Low prices
     */
    val low
        get() = lowSeries.toDoubleArray()

    /**
     * Close prices
     */
    val close
        get() = closeSeries.toDoubleArray()

    /**
     * Volume
     */
    val volume
        get() = volumeSeries.toDoubleArray()

    /**
     * Typical prices ( high + low + close / 3)
     */
    val typical
        get() = (highSeries.toDoubleArray() + lowSeries.toDoubleArray() + closeSeries.toDoubleArray()) / 3.0

    /**
     * Update the buffer with a new [priceBar]
     */
    fun add(priceBar: PriceBar): Boolean {
        return add(priceBar.ohlcv)
    }

    /**
     * Update the buffer with a new [ohlcv] values
     */
    fun add(ohlcv: DoubleArray): Boolean {
        assert(ohlcv.size == 5)
        openSeries.add(ohlcv[0])
        highSeries.add(ohlcv[1])
        lowSeries.add(ohlcv[2])
        closeSeries.add(ohlcv[3])
        volumeSeries.add(ohlcv[4])
        return isFilled()
    }

    /**
     * Is there enough data available
     */
    fun isFilled(): Boolean {
        return openSeries.isFilled()
    }

    /**
     * Clear all stored prices and volumes
     */
    fun clear() {
        openSeries.clear()
        highSeries.clear()
        lowSeries.clear()
        closeSeries.clear()
        volumeSeries.clear()
    }

}

/**
 * Multi asset price bar series that keeps a number of history price-bars in memory per asset. All the moving windows
 * have the same size.
 *
 * @property windowSize The number of historic price-bars per asset to keep
 * @constructor Create new Multi asset price bar series
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
     * Add all actions of the [PriceBar] found in the [event] to this series
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
     * Clear all captured prices
     */
    fun clear() = data.clear()

}

