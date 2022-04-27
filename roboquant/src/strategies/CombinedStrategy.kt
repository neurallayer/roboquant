/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.strategies

import org.roboquant.RunPhase
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
open class CombinedStrategy(val strategies: Collection<Strategy>) : Strategy {

    constructor(vararg strategies: Strategy) : this(strategies.toList())

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()

        for (strategy in strategies) {
            val s = strategy.generate(event)
            signals.addAll(s)
        }
        return signals
    }


    override fun start(runPhase: RunPhase) {
        for (strategy in strategies) strategy.start(runPhase)
    }

    override fun end(runPhase: RunPhase) {
        for (strategy in strategies) strategy.end(runPhase)
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