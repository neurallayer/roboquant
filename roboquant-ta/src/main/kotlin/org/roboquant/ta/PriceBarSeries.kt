/*
 * Copyright 2020-2023 Neural Layer
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

import org.roboquant.common.PriceSeries
import org.roboquant.common.div
import org.roboquant.common.plus
import org.roboquant.feeds.Action
import org.roboquant.feeds.PriceBar

/**
 * PriceBarSeries is a moving window of OHLCV values (PriceBar) of fixed capacity for a single asset.
 * So when the capacity is reached, this oldest entry will be removed.
 *
 * You can however increase the capacity using [increaseCapacity].
 *
 * @param capacity the size of buffer
 *
 * @constructor Create a new instance of PriceBarSeries
 */
class PriceBarSeries(capacity: Int) {

    // The individual buffers
    private val openBuffer = PriceSeries(capacity)
    private val highBuffer = PriceSeries(capacity)
    private val lowBuffer = PriceSeries(capacity)
    private val closeBuffer = PriceSeries(capacity)
    private val volumeBuffer = PriceSeries(capacity)

    /**
     * Open prices
     */
    val open
        get() = openBuffer.toDoubleArray()

    /**
     * High prices
     */
    val high
        get() = highBuffer.toDoubleArray()

    /**
     * Low prices
     */
    val low
        get() = lowBuffer.toDoubleArray()

    /**
     * Close prices
     */
    val close
        get() = closeBuffer.toDoubleArray()

    /**
     * Volume
     */
    val volume
        get() = volumeBuffer.toDoubleArray()

    /**
     * Typical prices (high + low + close / 3)
     */
    val typical
        get() = (highBuffer.toDoubleArray() + lowBuffer.toDoubleArray() + closeBuffer.toDoubleArray()) / 3.0

    /**
     * Update the buffer with a new [priceBar]
     */
    fun add(priceBar: PriceBar): Boolean {
        return add(priceBar.ohlcv)
    }

    /**
     * Update the buffer with a new [action], but only if the action is a price-bar.
     * Return true if a value has been added and it is full.
     */
    fun add(action: Action): Boolean {
        return if (action is PriceBar) {
            add(action.ohlcv)
        } else {
            false
        }
    }

    /**
     * Update the buffer with a new [ohlcv] values and true if series is full.
     */
    private fun add(ohlcv: DoubleArray): Boolean {
        assert(ohlcv.size == 5)
        openBuffer.add(ohlcv[0])
        highBuffer.add(ohlcv[1])
        lowBuffer.add(ohlcv[2])
        closeBuffer.add(ohlcv[3])
        volumeBuffer.add(ohlcv[4])
        return isFull()
    }

    /**
     * Returns the OHLCV values at the specified [index] as a [DoubleArray]
     */
    operator fun get(index: Int): DoubleArray =
        doubleArrayOf(open[index], high[index], low[index], close[index], volume[index])

    /**
     * Return true if there is enough data available.
     */
    fun isFull(): Boolean {
        return openBuffer.isFull()
    }

    /**
     * Return the size of this series
     */
    val size: Int
        get() = openBuffer.size

    /**
     * Clear all stored prices and volumes
     */
    fun clear() {
        openBuffer.clear()
        highBuffer.clear()
        lowBuffer.clear()
        closeBuffer.clear()
        volumeBuffer.clear()
    }

    /**
     * Aggregate [n] price-bars into a new PriceBarSeries and return the result.
     * If the stored price-bars have gaps in the timeline, you might not want to use this.
     *
     * Example:
     * ```
     * val priceBarSerie5Minutes = priceBarSerie1Minute.aggregate(5)
     * ```
     */
    fun aggregate(n: Int): PriceBarSeries {
        require(n > 0) { "number should be larger than 0" }
        val result = PriceBarSeries(size / n)
        for (i in 0 until size step n) {
            if (i + n > size) break
            val open = open[i]
            var total = 0.0
            var lowest = low[i]
            var highest = high[i]
            for (j in i until i + n) {
                if (low[j] < lowest) lowest = low[j]
                if (high[j] > highest) highest = high[j]
                total += volume[j]
            }
            val ohlcv = doubleArrayOf(open, highest, lowest, close[i + n - 1], total)
            result.add(ohlcv)
        }
        return result
    }

    /**
     * Set the capacity of the buffers to [newCapacity]. Existing stored values will be retained.
     */
    fun increaseCapacity(newCapacity: Int) {
        openBuffer.increaeseCapacity(newCapacity)
        highBuffer.increaeseCapacity(newCapacity)
        lowBuffer.increaeseCapacity(newCapacity)
        closeBuffer.increaeseCapacity(newCapacity)
        volumeBuffer.increaeseCapacity(newCapacity)
    }

}




