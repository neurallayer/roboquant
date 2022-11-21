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
import org.roboquant.feeds.Event
import java.util.*

/**
 * Moving Window that holds double values and is typically used by strategies for moving averages or replay buffers.
 *
 * Internally it uses a double[] to hold its values. Instances of this class are not thread safe during updating.
 *
 * @property windowSize The size of the moving window
 * @constructor Create empty Circular buffer
 */
open class MovingWindow(private val windowSize: Int) {

    private val data = DoubleArray(windowSize) { Double.NaN }
    private var counter = 0L

    /**
     * Add a new [value] to the end of the window
     */
    open fun add(value: Double) {
        val index = (counter % windowSize).toInt()
        data[index] = value
        counter++
    }

    /**
     * Return true if the history fully filled, so it is ready to be used.
     */
    fun isAvailable(): Boolean {
        return counter > windowSize
    }

    /**
     * Return the stored values a DoubleArray. If this is called before the window is completely filled, it will
     * contain Double.NaN values for the missing values.
     *
     * ## Usage
     *
     *      if (movingWindow.isAvailable()) return movingWindow.toDoubleArray()
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
     * Clear the state stored
     */
    open fun clear() {
        counter = 0L
        Arrays.fill(data, Double.NaN)
    }

}

/**
 * Add all the prices found in the event. If there is no entry yet, a new MovingWindow will be created
 */
fun MutableMap<Asset, MovingWindow>.addAll(event: Event, windowSize: Int, priceType: String = "DEFAULT") {
    for ((asset, action) in event.prices) {
        val movingWindow = getOrPut(asset) { MovingWindow(windowSize)}
        movingWindow.add(action.getPrice(priceType))
    }
}