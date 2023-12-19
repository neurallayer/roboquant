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

package org.roboquant.common

import org.roboquant.feeds.Event

/**
 * Holds a fix amount of historic prices. When adding a new value while the buffer is full, the oldest one will be
 * removed (aka a circular buffer). This is typically used by strategies to track rolling windows or replay buffers.
 *
 * Internally, it uses a DoubleArray to hold the price values. Instances of this class are not thread safe during
 * updates.
 *
 * @property capacity The number of historic prices to retain, aka the capacity of the buffer
 * @constructor Create a new instance of PriceSeries
 */
open class PriceSeries(private var capacity: Int) {

    private var data = DoubleArray(capacity)
    private var counter = 0L

    /**
     * Append a new [price] to the end of the buffer. If the buffer is full, the first element will be removed to make
     * room.
     *
     * Returns true is the buffer is full, false otherwise
     */
    open fun add(price: Double): Boolean {
        val index = (counter % capacity).toInt()
        data[index] = price
        counter++
        return isFull()
    }

    /**
     * Uodate the last price with a new [price].
     * Returns true is the buffer is full, false otherwise
     */
    open fun update(price: Double): Boolean {
        if (counter == 0L) throw NoSuchElementException()
        val index = ((counter - 1) % capacity).toInt()
        data[index] = price
        return isFull()
    }

    /**
     * Return the last added price, even if the buffer isn't filled yet
     */
    open fun last() : Double {
        if (counter == 0L) throw NoSuchElementException()
        val index = ((counter - 1) % capacity).toInt()
        return data[index]
    }

    /**
     * Return true if the rolling window is fully filled, so it is ready to be used.
     */
    fun isFull(): Boolean {
        return counter >= capacity
    }

    /**
     * return the size of this price series
     */
    val size: Int
        get() = if (counter > capacity) capacity else counter.toInt()

    /**
     * Return the stored values as a DoubleArray. If this method is invoked before the buffer is full, it will
     * return a smaller array of length [PriceSeries.size].
     *
     * ## Usage
     * ```
     * if (movingWindow.isFull()) return movingWindow.toDoubleArray()
     * ```
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(size)
        return if (counter > capacity) {
            val offset = (counter % capacity).toInt()
            System.arraycopy(data, offset, result, 0, capacity - offset)
            System.arraycopy(data, 0, result, capacity - offset, offset)
            result
        } else {
            System.arraycopy(data, 0, result, 0, result.size)
            result
        }
    }

    /**
     * Increase the capacity to the [newCapacity]
     */
    fun increaseCapacity(newCapacity: Int) {
        require(newCapacity > capacity)
        val oldData = toDoubleArray()
        data = DoubleArray(newCapacity)
        System.arraycopy(oldData, 0, data, 0, size)
        capacity = newCapacity
    }

    /**
     * Clear the buffer and reset the capacity to the initial [capacity]
     */
    open fun clear() {
        counter = 0L
        data = DoubleArray(capacity)
    }

}

/**
 * Add all the prices found in the event. If there is no entry yet, a new PriceSeries will be created
 */
fun MutableMap<Asset, PriceSeries>.addAll(event: Event, capacity: Int, priceType: String = "DEFAULT") {
    for ((asset, action) in event.prices) {
        val priceSeries = getOrPut(asset) { PriceSeries(capacity) }
        priceSeries.add(action.getPrice(priceType))
    }
}

