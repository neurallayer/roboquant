package org.roboquant.strategies.utils

import org.roboquant.feeds.PriceBar

/**
 * Calculate the VWAP for a series of [PriceBar] based on a fixed moving window. This can be used to track VWAP over
 * a longer period of time.
 *
 * @constructor Create new VWAP calculator
 */
class VWAPCalculator(windowSize: Int = 100) {

    val total = MovingWindow(windowSize)
    val volume = MovingWindow(windowSize)

    fun add(action: PriceBar) {
        val v = action.volume.toDouble()
        total.add(action.getPrice("TYPICAL") * v)
        volume.add(v)
    }

    fun isReady() = total.isAvailable()

    fun calc(): Double {
        return total.toDoubleArray().sum() / volume.toDoubleArray().sum()
    }

    fun clear() {
        total.clear()
        volume.clear()
    }


}