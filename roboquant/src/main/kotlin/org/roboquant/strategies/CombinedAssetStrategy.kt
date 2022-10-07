/*
 * Copyright 2020-2022 Neural Layer
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

import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.metrics.MetricResults

/**
 * Combine multiple strategies into one strategy. It is lazy in that if it encounters a new
 * asset for which it doesn't yet have a strategy for, it will invoke the provided [strategyBuilder].
 *
 * @property strategyBuilder
 * @constructor Create new Combined Asset Strategy
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
        val now = event.time
        event.prices.forEach { (asset, priceAction) ->
            val strategy = strategies.getOrPut(asset) { strategyBuilder(asset) }
            val newStep = Event(listOf(priceAction), now)
            val newSignals = strategy.generate(newStep)
            signals.addAll(newSignals)
        }
        return signals
    }

    override fun reset() {
        strategies.clear()
    }

    fun enableRecording(value: Boolean) {
        strategies.values.filterIsInstance<RecordingStrategy>().forEach {
            it.recording = value
        }
    }

    override fun getMetrics(): MetricResults {
        val result = mutableMapOf<String, Double>()
        strategies.values.forEach {
            result += it.getMetrics()
        }
        return result
    }
}