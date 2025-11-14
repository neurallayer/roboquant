/*
 * Copyright 2020-2025 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.roboquant.common.addNotNull
import org.roboquant.common.Event
import org.roboquant.common.PriceBar
import org.roboquant.common.Signal
import org.roboquant.common.SignalType
import org.roboquant.strategies.Strategy

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators from
 * the TaLib library.
 *
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 *  @property initialCapacity the initial capacity to track.
 *  If not enough history is available to calculate the indicators, the capacity will be automatically increased until
 *  it is able to perform the calculations.
 *  @property block the logic that will generate a signal.
 *  The provided block will only be called if the [initialCapacity] of historic prices has been filled.
 */
class TaLibSignalStrategy(
    private val initialCapacity: Int = 1,
    private var block: TaLib.(asset: Asset, series: PriceBarSeries) -> Signal?
) : Strategy {

    private val history = mutableMapOf<Asset, PriceBarSeries>()

    /**
     * The underlying [TaLib] instance that is used when executing this strategy.
     */
    val taLib = TaLib()

    /**
     * Some default TA strategies that are based off TaLibSignalStrategy
     */
    companion object {

        /**
         * Breakout strategy that supports different entry and exit periods
         */
        fun breakout(entryPeriod: Int = 100, exitPeriod: Int = 50): TaLibSignalStrategy {
            return TaLibSignalStrategy { asset, series ->
                when {
                    recordHigh(series.high, entryPeriod) -> Signal.buy(asset, SignalType.BOTH)
                    recordLow(series.low, entryPeriod) -> Signal.sell(asset, SignalType.BOTH)
                    recordLow(series.low, exitPeriod) -> Signal.sell(asset, SignalType.EXIT)
                    recordHigh(series.high, exitPeriod) -> Signal.buy(asset, SignalType.EXIT)
                    else -> null
                }
            }

        }

        /**
         * MACD strategy
         */
        fun macd(): TaLibSignalStrategy {

            val strategy = TaLibSignalStrategy { asset, prices ->
                val (_, _, diff) = macd(prices)
                val (_, _, diff2) = macd(prices, slowPeriod = 1)
                when {
                    diff < 0.0 && diff2 >= 0.0 -> Signal.buy(asset)
                    diff > 0.0 && diff2 <= 0.0 -> Signal.sell(asset)
                    else -> null
                }
            }

            return strategy
        }

        /**
         * Super trend strategy. The used formala is:
         *
         * ```
         * super-trend = (high + low) / 2 +  multiplier * ATR
         * ```
         */
        fun superTrend(period: Int = 14, multiplier: Double = 1.0): TaLibSignalStrategy {
            val strategy = TaLibSignalStrategy { asset, prices ->
                val atr = multiplier * atr(prices, period)
                val mid = (prices.high.last() + prices.low.last()) / 2.0
                val curr = prices.close.last()
                when {
                    mid + atr > curr -> Signal.buy(asset)
                    mid - atr < curr -> Signal.sell(asset)
                    else -> null
                }
            }
            return strategy
        }

    }

    /**
     * Based on a [event], return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @see Strategy.createSignals
     *
     */
    override fun createSignals(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        val time = event.time
        for (priceAction in event.prices.values.filterIsInstance<PriceBar>()) {
            val asset = priceAction.asset
            val buffer = history.getOrPut(asset) { PriceBarSeries(initialCapacity) }
            if (buffer.add(priceAction, time)) {
                try {
                    val signal = block.invoke(taLib, asset, buffer)
                    signals.addNotNull(signal)
                } catch (ex: InsufficientData) {
                    buffer.increaseCapacity(ex.minSize)
                }
            }
        }
        return signals
    }


    /**
     * @suppress
     */
    override fun toString() = "TaLibSignalStrategy"

}

