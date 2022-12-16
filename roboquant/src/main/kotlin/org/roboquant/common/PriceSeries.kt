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
 * @property capacity The number of historic prices to retain, aka the capacity of the buffer
 * @constructor Create new instance of PriceSeries
 */
open class PriceSeries(private val capacity: Int) {

    private val data = DoubleArray(capacity) { Double.NaN }
    private var counter = 0L

    /**
     * Append a new [price] to the end of the buffer. If the buffer is full, the first element will be removed to make
     * room. Return true is the buffer is full, false otherwise
     */
    open fun add(price: Double) : Boolean {
        val index = (counter % capacity).toInt()
        data[index] = price
        counter++
        return isFilled()
    }

    /**
     * Return true if the rolling window is fully filled, so it is ready to be used.
     */
    fun isFilled(): Boolean {
        return counter >= capacity
    }

    /**
     * return the size of this price series
     */
     val size: Int
        get() = if (counter > capacity) capacity else counter.toInt()


    /**
     * Return the stored values as a DoubleArray. If this is called before the window is full, it will
     * contain Double.NaN values for the missing values.
     *
     * ## Usage
     *
     *      if (movingWindow.isFilled()) return movingWindow.toDoubleArray()
     *
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(capacity)
        val offset = (counter % capacity).toInt()
        System.arraycopy(data, offset, result, 0, capacity - offset)
        System.arraycopy(data, 0, result, capacity - offset, offset)
        return result
    }

    /**
     * Clear the buffer
     */
    open fun clear() {
        counter = 0L
        Arrays.fill(data, Double.NaN)
    }

}

/**
 * Add all the prices found in the event. If there is no entry yet, a new PriceSeries will be created
 */
fun MutableMap<Asset, PriceSeries>.addAll(event: Event, capacity: Int, priceType: String = "DEFAULT") {
    for ((asset, action) in event.prices) {
        val priceSeries = getOrPut(asset) { PriceSeries(capacity)}
        priceSeries.add(action.getPrice(priceType))
    }
}

