package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.metrics.MetricResults

/**
 * Combine multiple strategies into one strategy. It is lazy in that if it encounters a new
 * asset for which it doesn't yet have a strategy for, it will invoke the provided builder.
 *
 * @property strategyBuilder
 * @constructor Create new Combined strategy
 */
class CombinedAssetStrategy(val strategyBuilder: (asset: Asset) -> Strategy) : Strategy {

    private val strategies = mutableMapOf<Asset, Strategy>()

    /**
     * Based onn event, return zero or more signals.
     *
     * @param event
     * @return List of signals
     */
    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        val now = event.now
        event.prices.forEach { (asset, priceAction) ->
            val strategy = strategies.getOrPut(asset) { strategyBuilder(asset) }
            val newStep = Event(listOf(priceAction), now)
            val newSignals = strategy.generate(newStep)
            signals.addAll(newSignals)
        }
        return signals
    }

    override fun start(phase: Phase) {
        strategies.clear()
    }

    fun enableRecording(value: Boolean) {
        strategies.values.filterIsInstance<RecordingStrategy>().forEach {
            it.recording = value
        }
    }

    override fun getMetrics(): MetricResults {
        val result = mutableMapOf<String, Number>()
        strategies.values.forEach {
            result += it.getMetrics()
        }
        return result
    }
}