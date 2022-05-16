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

@file:Suppress("unused", "LongParameterList")

package org.roboquant.ta

import org.roboquant.common.Logging
import org.roboquant.common.RoboquantException
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.strategies.Strategy
import org.roboquant.strategies.utils.MultiAssetPriceBarSeries
import org.roboquant.strategies.utils.PriceBarSeries
import java.lang.Integer.max
import java.util.logging.Logger

/**
 * This strategy that makes it easy to implement different types strategies based on technical analysis indicators.
 * This strategy requires [PriceBar] data and common use cases are candlestick patterns and moving average strategies.
 *
 * It is important that the strategy is initialized with a large enough history window to support the underlying
 * technical indicators you want to use. If the history is too small, it will lead to a runtime exception.
 *
 */
class TaLibStrategy(history: Int = 15) : Strategy {

    private var sellFn: TaLib.(series: PriceBarSeries) -> Boolean = { false }
    private var buyFn: TaLib.(series: PriceBarSeries) -> Boolean = { false }
    private val data = MultiAssetPriceBarSeries(history)
    private val logger: Logger = Logging.getLogger(TaLibStrategy::class)
    val taLib = TaLib()


    companion object {

        /**
         * When we hit a record high or low we generate a BUY or SELL signal. This implementation allows configuring
         * multiple periods, where any period that register a record high or low will be sufficient.
         *
         * The exact rules for a single period being:
         *
         * * Is the last price a record high compared to previous n-days, generate a BUY signal
         * * Is the last price a record low compared to previous n-days, generate a SELL signal
         * * Otherwise don't generate any signal
         *
         * @param timePeriods
         * @return
         */
        fun recordHighLow(vararg timePeriods: Int = intArrayOf(100)): TaLibStrategy {

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
         * SMA crossover
         *
         * @param slow
         * @param fast
         * @return
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
         * Vwap strategy buys if the VWAP is higher than the actual price by a certain margin and sells if the
         * vwap is lower than the actual price by a certain margin. Although this strategy can be used for any interval
         * between prices, it is most often used for day trading and small time-intervals between price events.
         *
         * @param period period to use to calculate the VWAP, default is 0.1%
         * @param bips the margin in Bips
         * @return
         */
        fun vwap(period: Int, bips: Int = 100) : TaLibStrategy {
            val percentage = bips / 10_000.0
            val strategy = TaLibStrategy(period)
            strategy.buy { vwap(it, period) > it.close.last() * (1.0 + percentage) }
            strategy.sell { vwap(it, period) < it.close.last() * (1.0 - percentage) }
            return strategy
        }

        /**
         * EMA crossover using the TaLib under the hood.
         *
         * See also [org.roboquant.strategies.EMACrossover] for more efficient implementation.
         *
         * @param slow
         * @param fast
         * @return
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
     *
     * # Example
     *
     *       strategy.buy { price ->
     *          ema(price.close, shortTerm) > ema(price.close, longTerm) && cdlMorningStar(price)
     *       }
     *
     */
    fun buy(block: TaLib.(series: PriceBarSeries) -> Boolean) {
        buyFn = block
    }

    /**
     * Define the sell conditions, return true if you want to generate a SELL signal, false otherwise
     *
     * # Example
     *
     *      strategy.sell { price ->
     *          cdl3BlackCrows(price) || cdl2Crows(price)
     *      }
     *
     */
    fun sell(block: TaLib.(series: PriceBarSeries) -> Boolean) {
        sellFn = block
    }

    /**
     * Based on a [event], return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @see Strategy.generate
     *
     */
    override fun generate(event: Event): List<Signal> {
        val results = mutableListOf<Signal>()
        for ((asset, priceAction) in event.prices) {
            if (priceAction is PriceBar && data.add(priceAction)) {
                try {
                    val series = data.getSeries(asset)
                    if (buyFn.invoke(taLib, series)) results.add(Signal(asset, Rating.BUY))
                    if (sellFn.invoke(taLib, series)) results.add(Signal(asset, Rating.SELL))
                } catch (e: InsufficientData) {
                    logger.severe("Not enough data available to calculate the indicators, increase history")
                    logger.severe(e.message)
                    throw e
                }
            }
        }
        return results
    }

    override fun reset() {
        data.clear()
    }

}

fun TaLib.recordLow(low: DoubleArray, period: Int, previous: Int = 0) =
    minIndex(low, period, previous) == low.lastIndex - previous

fun TaLib.recordLow(data: PriceBarSeries, period: Int, previous: Int = 0) = recordLow(data.low, period, previous)

fun TaLib.recordHigh(high: DoubleArray, period: Int, previous: Int = 0) =
    maxIndex(high, period, previous) == high.lastIndex - previous

fun TaLib.recordHigh(data: PriceBarSeries, period: Int, previous: Int = 0) = recordHigh(data.high, period, previous)

fun TaLib.vwap(
    high: DoubleArray,
    low: DoubleArray,
    close: DoubleArray,
    volume: DoubleArray,
    period: Int,
    previous: Int = 0
): Double {
    val end = high.lastIndex - previous
    var sumPrice = 0.0
    var sumVolume = 0.0
    val start = end - period + 1
    if (start < 0) throw InsufficientData(
        "insufficient data to calculate vwap, minimum lookback period is ${period + previous}"
    )

    for (i in start..end) {
        val typicalPrice = (close[i] + high[i] + low[i]) / 3
        sumPrice += typicalPrice * volume[i]
        sumVolume += volume[i]
    }
    return sumPrice / sumVolume
}

fun TaLib.vwap(series: PriceBarSeries, period: Int, previous: Int = 0): Double =
    vwap(series.high, series.low, series.close, series.volume, period, previous)

class InsufficientData(msg: String) : RoboquantException(msg)