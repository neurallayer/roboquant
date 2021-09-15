package org.roboquant.strategies.utils

import java.util.*

/**
 * Moving Window that holds double values and is typically used by strategies for moving averages or replay buffers.
 *
 * Internally it uses a double[] to hold its values. Instances of this class are not thread safe during updating.
 *
 * @property windowSize The size of the moving window
 * @constructor Create empty Circular buffer
 */
open class MovingWindow(val windowSize: Int)  {

    private val buffer = DoubleArray(windowSize) { Double.NaN }
    private var counter = 0L

    /**
     * Add a new value to the end of the window
     *
     * @param value
     */
    open fun add(value: Double) {
        val index = (counter % windowSize).toInt()
        buffer[index] = value
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
     * Return the stored values a DoubleArray. If this is called before the buffer is completely filled, it will
     * contain Double.NaN values for the missing part.
     *
     * ## Usage
     *
     *      if (movingWindow.isAvailable()) return movingWindow.toDoubleArray()
     *
     * @return the data as a DoubleArray
     */
    fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(windowSize)
        val offset = (counter % windowSize).toInt()
        System.arraycopy(buffer, offset, result, 0, windowSize - offset)
        System.arraycopy(buffer, 0, result, windowSize - offset, offset)
        return result
    }

    /**
     * Clear the state stored
     */
    open fun clear() {
        counter = 0L
        Arrays.fill(buffer, Double.NaN)
    }


}