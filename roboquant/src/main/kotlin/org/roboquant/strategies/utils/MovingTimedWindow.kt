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

import java.time.Instant
import java.util.*

/**
 * Moving Window that holds double values and the time and is typically used by strategies for moving averages or
 * replay buffers.
 *
 * Instances of this class are not thread safe when updating
 *
 * @property windowSize The size of the moving window
 * @constructor Create empty Circular buffer
 */
open class MovingTimedWindow(val windowSize: Int) {

    private val data = DoubleArray(windowSize) { Double.NaN }
    private val times = LongArray(windowSize) { Long.MIN_VALUE }
    private var counter = 0L

    /**
     * Add a new [value] to the end of the window with the specified [time].
     *
     * @param value
     */
    open fun add(value: Double, time: Instant) {
        val index = (counter % windowSize).toInt()
        data[index] = value
        times[index] = time.toEpochMilli()
        counter++
    }

    /**
     * Return true if the window fully filled, false otherwise.
     */
    fun isAvailable(): Boolean {
        return counter > windowSize
    }

    /**
     * Return the stored values as a DoubleArray
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(windowSize)
        val offset = (counter % windowSize).toInt()
        System.arraycopy(data, offset, result, 0, windowSize - offset)
        System.arraycopy(data, 0, result, windowSize - offset, offset)
        return result
    }

    /**
     * Return the stored times in a LongArray using [Instant.toEpochMilli]
     */
    fun toLongArray(): LongArray {
        val result = LongArray(windowSize)
        val offset = (counter % windowSize).toInt()
        System.arraycopy(times, offset, result, 0, windowSize - offset)
        System.arraycopy(times, 0, result, windowSize - offset, offset)
        return result
    }

    /**
     * Clear the state stored
     */
    open fun clear() {
        counter = 0L
        Arrays.fill(data, Double.NaN)
        Arrays.fill(times, Instant.MIN.toEpochMilli())
    }

}