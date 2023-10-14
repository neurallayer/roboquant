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

import org.roboquant.common.*
import org.roboquant.feeds.Action
import org.roboquant.feeds.PriceBar
import java.time.Instant

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
    private val timeBuffer = mutableListOf<Instant>()

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
     * Timeline
     */
    val timeline: Timeline
        get() = timeBuffer

    /**
     * Typical prices (high + low + close / 3)
     */
    val typical
        get() = (highBuffer.toDoubleArray() + lowBuffer.toDoubleArray() + closeBuffer.toDoubleArray()) / 3.0

    /**
     * Update the buffer with a new [priceBar] and optional the [time]
     * Return true if the buffer is full.
     */
    fun add(priceBar: PriceBar, time: Instant): Boolean {
        return add(priceBar.ohlcv, time)
    }

    /**
     * Update the buffer with a new [action], but only if the action is a price-bar.
     * Return true if a value has been added and it is full.
     */
    fun add(action: Action, time: Instant): Boolean {
        return if (action is PriceBar) {
            add(action.ohlcv, time)
        } else {
            false
        }
    }

    /**
     * Update the buffer with a new [ohlcv] values and optional the [time]. Return true if series is full.
     */
    private fun add(ohlcv: DoubleArray, time: Instant): Boolean {
        assert(ohlcv.size == 5)
        openBuffer.add(ohlcv[0])
        highBuffer.add(ohlcv[1])
        lowBuffer.add(ohlcv[2])
        closeBuffer.add(ohlcv[3])
        volumeBuffer.add(ohlcv[4])
        timeBuffer.add(time)
        while (timeBuffer.size > openBuffer.size) timeBuffer.removeFirst()
        return isFull()
    }

    /**
     * Returns the OHLCV values at the specified [index] as a [DoubleArray]
     */
    operator fun get(index: Int): DoubleArray =
        doubleArrayOf(open[index], high[index], low[index], close[index], volume[index])

    /**
     * Returns a PriceBarSeries that includes the data that occured within the provided [timeframe]
     */
    operator fun get(timeframe: Timeframe): PriceBarSeries {
        val tl = timeline.toTypedArray()
        val size = tl.filter { it in timeframe }.size
        val result = PriceBarSeries(size)
        for (i in 0..<size) {
            val time = tl[i]
            if (time in timeframe) result.add(get(i), time)
        }
        return result
    }

    /**
     * Returns the OHLCV values at the specified [time] as a [DoubleArray]. Throws [NoSuchElementException] if there
     * is no data is found at the requested [time]
     */
    operator fun get(time: Instant): DoubleArray {
        val index = timeBuffer.indexOf(time)
        if (index == -1) throw NoSuchElementException("time not found")
        return doubleArrayOf(open[index], high[index], low[index], close[index], volume[index])
    }

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
        timeBuffer.clear()
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
            val last = i + n - 1
            val ohlcv = doubleArrayOf(open, highest, lowest, close[last], total)
            result.add(ohlcv, timeline[last])
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




