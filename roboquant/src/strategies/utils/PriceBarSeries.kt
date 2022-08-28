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
import org.roboquant.feeds.PriceBar

/**
 * PriceBar Series is a moving window of OHLCV values for a single [asset].
 *
 * @constructor Create new PriceBar Series
 */
class PriceBarSeries(val asset: Asset, windowSize: Int) {

    private val openSeries = MovingWindow(windowSize)
    private val highSeries = MovingWindow(windowSize)
    private val lowSeries = MovingWindow(windowSize)
    private val closeSeries = MovingWindow(windowSize)
    private val volumeSeries = MovingWindow(windowSize)

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
        assert(priceBar.asset == asset)
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
        return isAvailable()
    }

    /**
     * Is there enough data available
     */
    fun isAvailable(): Boolean {
        return openSeries.isAvailable()
    }

    /**
     * Clear all captured prices
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
 * Multi asset price bar series that keeps a number of history price-bars in memory per asset.
 *
 * @property history The number of historic price-bars per asset to keep
 * @constructor Create new Multi asset price bar series
 */
class MultiAssetPriceBarSeries(private val history: Int) {

    private val data = mutableMapOf<Asset, PriceBarSeries>()

    /**
     * Add a new priceBar and return true if already enough data, false otherwise
     */
    fun add(priceBar: PriceBar): Boolean {
        val series = data.getOrPut(priceBar.asset) { PriceBarSeries(priceBar.asset, history) }
        return series.add(priceBar)
    }

    /**
     * Is there enough data captured for the provided [asset]
     */
    fun isAvailable(asset: Asset) = data[asset]?.isAvailable() ?: false

    /**
     * Get the price bar series for the provided [asset]
     *
     * @param asset
     */
    fun getSeries(asset: Asset) : PriceBarSeries = data.getValue(asset)

    /**
     * Clear all captured prices
     */
    fun clear() = data.clear()

}

