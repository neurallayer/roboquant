package org.roboquant.strategies.utils

import org.apache.commons.math3.stat.correlation.Covariance
import org.apache.commons.math3.stat.descriptive.moment.Variance


class PairBuffer(size: Int, usePercentage: Boolean = true) {

    private val buffer1 = if (usePercentage) PercentageMovingWindow(size) else MovingWindow(size)
    private val buffer2 = if (usePercentage) PercentageMovingWindow(size) else MovingWindow(size)

    /**
     * Add new values to the end of the buffers
     *
     * @param value1
     */
    fun add(value1: Double, value2: Double) {
        buffer1.add(value1)
        buffer2.add(value2)
    }

    fun isAvailable(): Boolean {
        return buffer1.isAvailable() && buffer2.isAvailable()
    }

    fun covariance(): Double {
        val data1 = buffer1.toDoubleArray()
        val data2 = buffer2.toDoubleArray()
        return Covariance().covariance(data1, data2)
    }

    fun beta(): Double {
        val data1 = buffer1.toDoubleArray()
        val data2 = buffer2.toDoubleArray()
        val c = Covariance().covariance(data1, data2)
        return c / Variance().evaluate(data1)
    }


    /**
     * Clear the buffer
     */
    fun clear() {
        buffer1.clear()
        buffer2.clear()
    }

}