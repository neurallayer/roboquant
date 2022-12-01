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

package org.roboquant.common

import org.roboquant.feeds.Event
import java.util.*

/**
 * Holds a fix amount of historic prices. When adding a new value while the buffer is full, the
 * oldest one will be removed. This is typically used by strategies for rolling windows or replay buffers
 *
 * Internally it uses a DoubleArray to hold the price values. Instances of this class are not thread safe
 * during updates.
 *
 * @property windowSize The size of window
 * @constructor Create new instance of PriceSeries
 */
open class PriceSeries(private val windowSize: Int) {

    private val data = DoubleArray(windowSize) { Double.NaN }
    private var counter = 0L

    /**
     * Add a new [price] to the end. If the window is full, the first element will be removed.
     * Return true is the series is already completely filled, false otherwise
     */
    open fun add(price: Double) : Boolean {
        val index = (counter % windowSize).toInt()
        data[index] = price
        counter++
        return isFilled()
    }

    /**
     * Return true if the rolling window is fully filled, so it is ready to be used.
     */
    fun isFilled(): Boolean {
        return counter > windowSize
    }

    /**
     * Return the stored values as a DoubleArray. If this is called before the window is completely filled, it will
     * contain Double.NaN values for the missing values.
     *
     * ## Usage
     *
     *      if (movingWindow.isFilled()) return movingWindow.toDoubleArray()
     *
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(windowSize)
        val offset = (counter % windowSize).toInt()
        System.arraycopy(data, offset, result, 0, windowSize - offset)
        System.arraycopy(data, 0, result, windowSize - offset, offset)
        return result
    }

    /**
     * Clear the state stored and set all values back to Double.NaN
     */
    open fun clear() {
        counter = 0L
        Arrays.fill(data, Double.NaN)
    }

}

/**
 * Add all the prices found in the event. If there is no entry yet, a new PriceSeries will be created
 */
fun MutableMap<Asset, PriceSeries>.addAll(event: Event, windowSize: Int, priceType: String = "DEFAULT") {
    for ((asset, action) in event.prices) {
        val priceSeries = getOrPut(asset) { PriceSeries(windowSize)}
        priceSeries.add(action.getPrice(priceType))
    }
}

