package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.common.Asset
import org.roboquant.common.addNotNull
import org.roboquant.feeds.Event
import org.roboquant.strategies.utils.MovingWindow
import org.roboquant.strategies.utils.PercentageMovingWindow

/**
 * Base class for strategies that are interested in historic prices or returns.
 *
 * @property period
 * @property priceType
 * @property useReturns
 * @constructor Create empty Historic price strategy
 */
abstract class HistoricPriceStrategy(
    val period: Int,
    val priceType: String = "DEFAULT",
    val useReturns: Boolean = false
) : RecordingStrategy() {

    private val history = mutableMapOf<Asset, MovingWindow>()

    override fun generate(event: Event): List<Signal> {
        val result = mutableListOf<Signal>()
        for ((asset, action) in event.prices) {
            val movingWindow =
                history.getOrPut(asset) { if (useReturns) PercentageMovingWindow(period) else MovingWindow(period) }
            movingWindow.add(action.getPrice(priceType))
            if (movingWindow.isAvailable()) {
                val data = movingWindow.toDoubleArray()
                assert(data.size == period)
                val signal = generate(asset, movingWindow.toDoubleArray())
                result.addNotNull(signal)
            }
        }
        return result
    }

    /**
     * Generate a signal based on the provided signal and asset
     *
     * @param asset
     * @param data
     * @return
     */
    abstract fun generate(asset: Asset, data: DoubleArray): Signal?

    override fun start(phase: Phase) {
        super.start(phase)
        history.clear()
    }

    override fun reset() {
        super.reset()
        history.clear()
    }
}