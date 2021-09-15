package org.roboquant.strategies.utils

import org.roboquant.common.div
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * PriceBar buffer is a buffer of the OHLCV. It supports both storing the regular prices and percentage difference
 *
 * @property windowSize
 * @constructor Create new PriceBar buffer
 */
class PriceBarBuffer(val windowSize: Int, usePercentage: Boolean = false) {

    private val openBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val highBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val lowBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val closeBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)
    private val volumeBuffer = if (usePercentage) PercentageMovingWindow(windowSize) else MovingWindow(windowSize)

    var now: Instant = Instant.MIN

    val open
        get() = openBuffer.toDoubleArray()

    val high
        get() = highBuffer.toDoubleArray()

    val low
        get() = lowBuffer.toDoubleArray()

    val close
        get() = closeBuffer.toDoubleArray()

    val volume
        get() = volumeBuffer.toDoubleArray()

    val typical
        get() = (highBuffer.toDoubleArray() + lowBuffer.toDoubleArray() + closeBuffer.toDoubleArray()) / 3.0


    /**
     * Update the buffer with a new price bar
     *
     * @param priceBar
     * @param now
     */
    fun update(priceBar: PriceBar, now: Instant) {
        openBuffer.add(priceBar.open.toDouble())
        highBuffer.add(priceBar.high.toDouble())
        lowBuffer.add(priceBar.low.toDouble())
        closeBuffer.add(priceBar.close.toDouble())
        volumeBuffer.add(priceBar.volume.toDouble())
        this.now = now
    }

    fun isAvailable(): Boolean {
        return openBuffer.isAvailable()
    }

    fun clear() {
        openBuffer.clear()
        highBuffer.clear()
        lowBuffer.clear()
        closeBuffer.clear()
        volumeBuffer.clear()
        now = Instant.MIN
    }

}

