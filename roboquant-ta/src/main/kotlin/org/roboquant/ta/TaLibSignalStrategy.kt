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

@file:Suppress("WildcardImport")

package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.addNotNull
import org.roboquant.common.severe
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.SignalType
import org.roboquant.strategies.Strategy
import org.roboquant.strategies.utils.PriceBarSeries
import java.lang.Integer.max
import java.util.logging.Logger

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators from
 * the TaLib library.
 *
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 * It is important that the strategy is initialized with a large enough [history] window to support the underlying
 * technical indicators you want to use. If the [history] is too small, it will lead to a runtime exception.
 *
 */
class TaLibSignalStrategy(
    private val history: Int = 15,
    private var block: TaLib.(series: PriceBarSeries) -> Signal?
) : Strategy {

    private val buffers = mutableMapOf<Asset, PriceBarSeries>()
    private val logger: Logger = Logging.getLogger(TaLibSignalStrategy::class)
    val taLib = TaLib()

    companion object {

        fun breakout(entryPeriod: Int = 100, exitPeriod: Int = 50): TaLibSignalStrategy {
            val maxPeriod = max(entryPeriod, exitPeriod)
            return TaLibSignalStrategy(maxPeriod) { series ->
                when {
                    recordHigh(series.high, entryPeriod) -> Signal(series.asset, Rating.BUY, SignalType.BOTH)
                    recordLow(series.low, entryPeriod) -> Signal(series.asset, Rating.SELL, SignalType.BOTH)
                    recordLow(series.low, exitPeriod) -> Signal(series.asset, Rating.SELL, SignalType.EXIT)
                    recordHigh(series.high, exitPeriod) -> Signal(series.asset, Rating.BUY, SignalType.EXIT)
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
        for (priceAction in event.prices.values.filterIsInstance<PriceBar>()) {
            val asset = priceAction.asset
            val buffer = buffers.getOrPut(asset) { PriceBarSeries(asset, history) }
            if (buffer.add(priceAction)) {
                try {
                    val signal = block.invoke(taLib, buffer)
                    signals.addNotNull(signal)
                } catch (e: InsufficientData) {
                    logger.severe(
                        "Not enough data available to calculate the indicators, increase the history size",
                        e
                    )
                    throw e
                }
            }
        }
        return signals
    }

    override fun reset() {
        buffers.clear()
    }

    override fun toString() = "TaLibSignalStrategy history=$history"

}

