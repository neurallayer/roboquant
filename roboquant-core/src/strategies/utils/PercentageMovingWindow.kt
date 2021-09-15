package org.roboquant.strategies.utils

import org.roboquant.common.Asset
import org.roboquant.feeds.Event

/**
 * Behaves like a regular [MovingWindow] but this implementation stores the percentage change and not the actually
 * passed value. The percentage is calculated using the following formula:
 *
 *      percentage = (current_value - past_value) / past_value
 *
 * Please note that it will take one additional value for the window to get filled, so a window size of 20 will take
 * 21 values to get filled and be ready.
 *
 * @property missingValue the value to store if no price is available for an asset at a certain step, default is NaN. The
 *  consequence is that fill you have many missing values, strategies might become unpredictable. An alternative is to
 *  set the default to 0.0 (so the return is 0 is unknown).
 * @param size The window size to use
 *
 * @constructor
 *
 *
 */
class PercentageMovingWindow(size: Int, private val missingValue: Double = Double.NaN) : MovingWindow(size) {

    var last: Double = Double.NaN

    override fun add(value: Double) {
        val diff = (value - last) / last
        if (diff.isNaN()) {
            super.add(missingValue)
            if (!value.isNaN()) last = value
        } else {
            super.add(diff)
            last = value
        }
    }

    override fun clear() {
        last = Double.NaN
        super.clear()
    }
}


/**
 *
 * @property assets the assets that need to be tracked
 * @param windowSize the moving window size to track
 * @param missingValue the value to use for returns if no price is available for an asset at a certain step
 * @constructor
 *
 * @param windowSize
 */
class AssetReturns(val assets: Collection<Asset>, windowSize: Int, missingValue: Double = 0.0) {

    private val buffers = mutableMapOf<Asset, PercentageMovingWindow>()

    init {
        for (asset in assets) {
            buffers[asset] = PercentageMovingWindow(windowSize, missingValue)
        }
    }

    fun isAvailable() = buffers.values.first().isAvailable()

    fun toDoubleArray(asset: Asset) = buffers[asset]!!.toDoubleArray()

    /**
     * Update the buffers with the latest prices
     *
     * @param event
     */
    fun add(event: Event) {
        val prices = event.prices
        buffers.forEach {
            val price = prices[it.key]?.getPrice() ?: Double.NaN
            it.value.add(price)
        }
    }

    /**
     * Clear the state
     */
    fun clear() {
        for (buffer in buffers.values) buffer.clear()
    }

}