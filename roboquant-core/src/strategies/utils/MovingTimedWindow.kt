package org.roboquant.strategies.utils

import java.time.Instant
import java.util.*




/**
 * Moving Window that holds double values an the time and is typically used by strategies for moving averages or
 * replay buffers.
 *
 * Instances of this class are not thread safe when updating
 *
 * @property windowSize The size of the moving window
 * @constructor Create empty Circular buffer
 */
open class MovingTimedWindow(val windowSize: Int)  {

    private val data = DoubleArray(windowSize) { Double.NaN }
    private val times = LongArray(windowSize) { Long.MIN_VALUE }
    private var counter = 0L

    /**
     * Add a new value to the end of the window
     *
     * @param value
     */
    open fun add(value: Double, now: Instant) {
        val index = (counter % windowSize).toInt()
        data[index] = value
        times[index] = now.toEpochMilli()
        counter++
    }

    /**
     * Is the window fully filled, so it is ready to be used.
     *
     * @return True if the window is filled, false otherwise
     */
    fun isAvailable(): Boolean {
        return counter > windowSize
    }


    /**
     * Return the stored values a DoubleArray
     *
     * @return the window as a Double array
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(windowSize)
        val offset = (counter % windowSize).toInt()
        System.arraycopy(data, offset, result, 0, windowSize - offset)
        System.arraycopy(data, 0, result, windowSize - offset, offset)
        return result
    }

    /**
     * Return the stored times in a LongArray (instant.toEpochMilli())
     *
     * @return the time as a Double array
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