/*
 * Copyright 2020-2023 Neural Layer
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

@file:Suppress("LongParameterList")

package org.roboquant.ta

import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.RecordingStrategy
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import java.lang.Integer.max

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators.
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 * It is important that the strategy is initialized with a large enough history window to support the underlying
 * technical indicators you want to use. If the history is too small, it will lead to a runtime exception.
 *
 * @param history the amount of history to track
 */
class TaLibStrategy(history: Int = 15) : RecordingStrategy(recording = true) {

    private var sellFn: TaLib.(series: PriceBarSerie) -> Boolean = { false }
    private var buyFn: TaLib.(series: PriceBarSerie) -> Boolean = { false }
    private val priceBarSeries = PriceBarSeries(history)

    /**
     * The underlying TaLib that will be used to run the strategy
     */
    val taLib = TaLib()

    /**
     * Contains several popular predefined strategies.
     */
    companion object Factory {

        /**
         * When we hit a record high or low, we generate a BUY or SELL signal. This implementation supports multiple
         * periods, where any period that registers a record high or low will be sufficient to generate a signal.
         *
         * The exact rules for a single period are:
         *
         * * Is the last price a record high compared to previous n-days, generate a BUY signal
         * * Is the last price a record low compared to previous n-days, generate a SELL signal
         * * Otherwise don't generate any signal
         *
         * @param timePeriods
         * @return
         */
        fun recordHighLow(vararg timePeriods: Int): TaLibStrategy {

            require(timePeriods.isNotEmpty()) { "At least one period needs to be provided" }
            require(timePeriods.all { it > 1 }) { "Any provided period needs to be at least of size 2" }

            val strategy = TaLibStrategy(timePeriods.maxOrNull()!!)
            strategy.buy {
                val data = it.high
                timePeriods.any { period -> recordHigh(data, period) }
            }
            strategy.sell {
                val data = it.low
                timePeriods.any { period -> recordLow(data, period) }
            }
            return strategy
        }

        /**
         * Breakout strategy generates a BUY signal if it is a records high over last [highPeriod] and
         * generates a SELL signal is it is a record low over last [lowPeriod].
         */
        fun breakout(highPeriod: Int = 100, lowPeriod: Int = 50): TaLibStrategy {
            require(highPeriod > 0 && lowPeriod > 0) { "Periods have to be larger than 0" }
            val strategy = TaLibStrategy(max(highPeriod, lowPeriod))
            strategy.buy {
                recordHigh(it.high, highPeriod)
            }
            strategy.sell {
                recordLow(it.low, lowPeriod)
            }

            return strategy
        }

        /**
         * Returns a SMA crossover strategy with the provided [slow] and [fast] times
         */
        fun smaCrossover(slow: Int, fast: Int): TaLibStrategy {
            require(slow > 0 && fast > 0) { "Periods have to be larger than 0" }
            require(slow > fast) { "Slow period have to be larger than fast period" }

            val strategy = TaLibStrategy(slow)
            strategy.buy { sma(it.close, fast) > sma(it.close, slow) }
            strategy.sell { sma(it.close, fast) < sma(it.close, slow) }
            return strategy
        }

        /**
         * Returns an EMA crossover Strategy using the provided [slow] and [fast] times.
         *
         * See also [org.roboquant.strategies.EMAStrategy] for more efficient implementation.
         */
        fun emaCrossover(slow: Int, fast: Int): TaLibStrategy {
            require(slow > 0 && fast > 0) { "Periods have to be larger than 0" }
            require(slow > fast) { "Slow period have to be larger than fast period" }

            val strategy = TaLibStrategy(slow)
            strategy.buy { ema(it.close, fast) > ema(it.close, slow) }
            strategy.sell { ema(it.close, fast) < ema(it.close, slow) }
            return strategy
        }

        /**
         * Strategy using the Relative Strength Index of an asset to generate signals. RSI measures the magnitude of
         * recent price changes to evaluate overbought or oversold conditions in the price of a stock or other asset.
         *
         * If the RSI rises above the configured high threshold (default 70), a sell signal will be generated. And if
         * the RSI falls below the configured low threshold (default 30), a buy signal will be generated.
         *
         * The period determines over which period the rsi is calculated
         *
         * @return
         */
        fun rsi(
            timePeriod: Int,
            lowThreshold: Double = 30.0,
            highThreshold: Double = 70.0
        ): TaLibStrategy {
            require(lowThreshold in 0.0..100.0 && highThreshold in 0.0..100.0) {
                "Thresholds have to be in the range 0..100"
            }
            require(highThreshold > lowThreshold) { "High threshold has to be larger than low threshold" }

            val strategy = TaLibStrategy(timePeriod + 1)
            strategy.buy { rsi(it.close, timePeriod) < lowThreshold }
            strategy.sell { rsi(it.close, timePeriod) > highThreshold }
            return strategy
        }

    }

    /**
     * Define the buy condition, return true if you want to generate a BUY signal, false otherwise
     * # Example
     * ```
     * strategy.buy { price ->
     *      ema(price.close, shortTerm) > ema(price.close, longTerm) && cdlMorningStar(price)
     * }
     * ```
     */
    fun buy(block: TaLib.(series: PriceBarSerie) -> Boolean) {
        buyFn = block
    }

    /**
     * Define the sell conditions, return true if you want to generate a SELL signal, false otherwise
     *
     * # Example
     * ```
     * strategy.sell { price ->
     *      cdl3BlackCrows(price) || cdl2Crows(price)
     * }
     * ```
     */
    fun sell(block: TaLib.(series: PriceBarSerie) -> Boolean) {
        sellFn = block
    }

    /**
     * Based on a [event], return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @see Strategy.generate
     */
    override fun generate(event: Event): List<Signal> {
        val results = mutableListOf<Signal>()
        for (priceBar in event.actions.filterIsInstance<PriceBar>()) {
            if (priceBarSeries.add(priceBar)) {
                val asset = priceBar.asset
                val priceSerie = priceBarSeries.getValue(asset)
                if (buyFn.invoke(taLib, priceSerie)) results.add(Signal(asset, Rating.BUY))
                if (sellFn.invoke(taLib, priceSerie)) results.add(Signal(asset, Rating.SELL))
            }
        }
        return results
    }

    /**
     * Reset all the history state
     */
    override fun reset() {
        priceBarSeries.clear()
    }

}

/**
 * Indicator for detecting record low based on an array with [low] historic prices
 */
fun TaLib.recordLow(low: DoubleArray, period: Int, previous: Int = 0) =
    minIndex(low, period, previous) == low.lastIndex - previous

/**
 * Indicator for detecting record based on historic priceBar [series]
 */
fun TaLib.recordLow(series: PriceBarSerie, period: Int, previous: Int = 0) = recordLow(series.low, period, previous)

/**
 * Indicator for detecting record high based on an array with [high] historic prices
 */
fun TaLib.recordHigh(high: DoubleArray, period: Int, previous: Int = 0) =
    maxIndex(high, period, previous) == high.lastIndex - previous

/**
 * Indicator for detecting record high based on an array with historic [series]
 */
fun TaLib.recordHigh(series: PriceBarSerie, period: Int, previous: Int = 0) =
    recordHigh(series.high, period, previous)

