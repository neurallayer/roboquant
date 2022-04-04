/*
 * Copyright 2021 Neural Layer
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
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar

/**
 * PriceBar Series is a circular buffer of OHLCV values. It supports both storing the regular prices or thre returns
 *
 * @constructor Create new PriceBar buffer
 */
class PriceBarSeries(windowSize: Int, usePercentage: Boolean = false) {

    private val openBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val highBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val lowBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val closeBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val volumeBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)

    val open
        get() = openBuffer.toDoubleArray()

    val high
        get() = highBuffer.toDoubleArray()

    val low
        get() = lowBuffer.toDoubleArray()

    val close
        get() = closeBuffer.toDoubleArray()

    val volume
        get() = volumeBuffer.toDoubleArray()

    val typical
        get() = (highBuffer.toDoubleArray() + lowBuffer.toDoubleArray() + closeBuffer.toDoubleArray()) / 3.0


    /**
     * Update the buffer with a new [priceBar]
     */
    fun add(priceBar: PriceBar) {
       add(priceBar.ohlcv)
    }

    /**
     * Update the buffer with a new [ohlcv] values
     */
    fun add(ohlcv: DoubleArray) {
        assert(ohlcv.size == 5)
        openBuffer.add(ohlcv[0])
        highBuffer.add(ohlcv[1])
        lowBuffer.add(ohlcv[2])
        closeBuffer.add(ohlcv[3])
        volumeBuffer.add(ohlcv[4])
    }

    fun isAvailable(): Boolean {
        return openBuffer.isAvailable()
    }

    fun clear() {
        openBuffer.clear()
        highBuffer.clear()
        lowBuffer.clear()
        closeBuffer.clear()
        volumeBuffer.clear()
    }

}

/**
 * Multi asset price bar series that keeps a number of history pricebars in memory per asset.
 *
 * @property history The number of historic pricebars per asset to keep
 * @constructor Create new Multi asset price bar series
 */
class MultiAssetPriceBarSeries(private val history: Int) {

    private val data = mutableMapOf<Asset, PriceBarSeries>()

    /**
     * Add a new priceBar and return true if enough data, false otherwise
     */
    fun add(priceBar: PriceBar): Boolean {
        val series = data.getOrPut(priceBar.asset) { PriceBarSeries(history) }
        series.add(priceBar)
        return series.isAvailable()
    }

    fun add(event: Event) {
        for ((_, action) in event.prices) {
            if (action is PriceBar) add(action)
        }
    }

    fun isAvailable(asset: Asset) = data[asset]?.isAvailable() ?: false

    fun getSeries(asset: Asset) = data.getValue(asset)

    fun clear() = data.clear()

}

