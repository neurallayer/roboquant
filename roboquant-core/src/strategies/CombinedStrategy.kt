package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.feeds.Event
import org.roboquant.metrics.MetricResults

/**
 * Combine the output of several strategies into a single list of signals. There is no logic included to filter
 * conflicting signals, like simultaneously BUY and SELL signals for the same asset. Also, the strategies are
 * run sequential. For parallel execution see [ParallelStrategy]
 *
 * @property strategies
 * @constructor Create empty Combined strategy
 */
open class CombinedStrategy(vararg val strategies: Strategy) : Strategy {

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()

        for (strategy in strategies) {
            val s = strategy.generate(event)
            signals.addAll(s)
        }
        return signals
    }


    override fun start(phase: Phase) {
        for (strategy in strategies) strategy.start(phase)
    }

    override fun end(phase: Phase) {
        for (strategy in strategies) strategy.end(phase)
    }

    override fun reset() {
        for (strategy in strategies) strategy.reset()
    }

    override fun getMetrics(): MetricResults {
        val result = mutableMapOf<String, Number>()
        strategies.forEach { result += it.getMetrics() }
        return result
    }
}