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

@file:Suppress("unused")

package org.roboquant.strategies

import org.roboquant.RunPhase
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.addNotNull
import org.roboquant.common.severe
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.MetricResults
import org.roboquant.strategies.ta.TALib
import org.roboquant.strategies.utils.PriceBarBuffer
import java.lang.Integer.max
import java.util.logging.Logger

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators.
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 * It is important that the strategy is initialized with a large enough [history] window to support the underlying
 * technical indicators you want to use. If the [history] is too small, it will lead to a runtime exception.
 *
 */
class TASignalStrategy(
    private val history: Int = 15,
    private var block: TASignalStrategy.(price: PriceBarBuffer, asset: Asset) -> Signal?
) : Strategy {

    private val buffers = mutableMapOf<Asset, PriceBarBuffer>()
    private val logger: Logger = Logging.getLogger(TAStrategy::class)
    val ta = TALib

    private var metrics = mutableMapOf<String, Number>()

    /**
     * Record a new metric. If there is already a metric recorded with the same key, it will be overwritten.
     *
     * @param key
     * @param value
     */
    fun record(key: String, value: Number) {
        metrics[key] = value
    }

    /**
     * Get the recorded metrics. After this method has been invoked, the metrics are also cleared, so calling this method
     * twice in a row won't return the same result.
     *
     * @return
     */
    override fun getMetrics(): MetricResults {
        val result = metrics
        metrics = mutableMapOf()
        return result
    }


    companion object {

        fun breakout(entryPeriod: Int = 100, exitPeriod: Int = 50): TASignalStrategy {
            val maxPeriod = max(entryPeriod, exitPeriod)
            return TASignalStrategy(maxPeriod) { data, asset ->
                when {
                    ta.recordHigh(data.high, entryPeriod) -> Signal(asset, Rating.BUY, SignalType.BOTH)
                    ta.recordLow(data.low, entryPeriod) -> Signal(asset, Rating.SELL, SignalType.BOTH)
                    ta.recordLow(data.low, exitPeriod) -> Signal(asset, Rating.SELL, SignalType.EXIT)
                    ta.recordHigh(data.high, exitPeriod) -> Signal(asset, Rating.BUY, SignalType.EXIT)
                    else -> null
                }
            }

        }


    }


    /**
     * Based on a [event], return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @see Strategy.generate
     *
     */
    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        val now = event.time
        for ((asset, priceAction) in event.prices) {
            if (priceAction is PriceBar) {
                val buffer = buffers.getOrPut(asset) { PriceBarBuffer(history, usePercentage = false) }
                buffer.update(priceAction, now)
                if (buffer.isAvailable()) {
                    try {
                        val signal = block.invoke(this, buffer, asset)
                        signals.addNotNull(signal)
                    } catch (e: InsufficientData) {
                        logger.severe("Not enough data available to calculate the indicators, increase the history size", e)
                        throw e
                    }
                }
            }
        }
        return signals
    }

    override fun start(runPhase: RunPhase) {
        super.start(runPhase)
        buffers.clear()
        metrics = mutableMapOf()
    }


    override fun toString() = "TASignalStrategy $history"

}

