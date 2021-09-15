@file:Suppress("unused")

package org.roboquant.strategies

import org.roboquant.Phase
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.MetricRecorder
import org.roboquant.strategies.ta.TALib
import org.roboquant.strategies.utils.PriceBarBuffer
import java.util.logging.Logger

/**
 * Strategy that makes it easy to implement different types strategies based on of technical analysis indicators.
 * This strategy requires [PriceBar] data and common use cases are candle stick patterns and moving average strategies.
 *
 * It is important that the strategy is initialized with a large enough history window to support the underlying
 * technical indicators you want to use.
 *
 * @property history The history to keep
 * @constructor Create a new TAStrategy
 */
class TAStrategy(private val history: Int = 100) : Strategy {

    private var sellFn: TAStrategy.(price: PriceBarBuffer) -> Boolean = { false }
    private var buyFn: TAStrategy.(price: PriceBarBuffer) -> Boolean = { false }
    private val buffers = mutableMapOf<Asset, PriceBarBuffer>()
    private val logger: Logger = Logging.getLogger(this)
    val ta = TALib

    private val recorder = MetricRecorder("ta.", true)

    fun record(key: String, value: Number) = recorder.record(key, value)
    override fun getMetrics() = recorder.getMetrics()

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
        fun recordHighLow(vararg timePeriods: Int = intArrayOf(200)): TAStrategy {

            require(timePeriods.isNotEmpty()) { "At least one period needs to be provided" }
            require(timePeriods.all { it > 1 }) { "Any provided period needs to be at least of size 2" }

            val strategy = TAStrategy(timePeriods.maxOrNull()!!)
            strategy.buy {
                val data = it.high
                timePeriods.any { period -> ta.recordHigh(data, period) }
            }
            strategy.sell {
                val data = it.low
                timePeriods.any { period -> ta.recordLow(data, period) }
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
        fun smaCrossover(slow: Int, fast: Int): TAStrategy {
            require(slow > 0 && fast > 0) { "Periods have to be larger than 0" }
            require(slow > fast) { "Slow period have to be larger than fast period" }

            val strategy = TAStrategy(slow)
            strategy.buy { ta.sma(it.close, fast) > ta.sma(it.close, slow) }
            strategy.sell { ta.sma(it.close, fast) < ta.sma(it.close, slow) }
            return strategy
        }

        /**
         * EMA crossover
         *
         * @param slow
         * @param fast
         * @return
         */
        fun emaCrossover(slow: Int, fast: Int): TAStrategy {
            require(slow > 0 && fast > 0) { "Periods have to be larger than 0" }
            require(slow > fast) { "Slow period have to be larger than fast period" }

            val strategy = TAStrategy(slow)
            strategy.buy { ta.ema(it.close, fast) > ta.ema(it.close, slow) }
            strategy.sell { ta.ema(it.close, fast) < ta.ema(it.close, slow) }
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
        ): TAStrategy {
            require(lowThreshold in 0.0..100.0 && highThreshold in 0.0..100.0) { "Thresholds have to be in the range 0..100" }
            require(highThreshold > lowThreshold) { "High threshold has to be larger than low threshold" }

            val strategy = TAStrategy(timePeriod + 1)
            strategy.buy { ta.rsi(it.close, timePeriod) < lowThreshold }
            strategy.sell { ta.rsi(it.close, timePeriod) > highThreshold }
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
     * @param block
     * @receiver
     */
    fun buy(block: TAStrategy.(price: PriceBarBuffer) -> Boolean) {
        buyFn = block
    }

    /**
     * Define the sell condition, return true if you want to generate a SELL signal, false otherwise
     *
     * # Example
     *
     *      strategy.sell { price ->
     *          cdl3BlackCrows(price) || cdl2Crows(price)
     *      }
     *
     * @param block
     * @receiver
     */
    fun sell(block: TAStrategy.(price: PriceBarBuffer) -> Boolean) {
        sellFn = block
    }

    /**
     * Based on a step, return zero or more signals. Typically, they are for the assets in the event,
     * but this is not a strict requirement.
     *
     * @param event
     * @return List of signals
     */
    override fun generate(event: Event): List<Signal> {
        val results = mutableListOf<Signal>()
        val now = event.now
        for ((asset, priceAction) in event.prices) {
            if (priceAction is PriceBar) {
                val buffer = buffers.getOrPut(asset) { PriceBarBuffer(history, usePercentage = false) }
                buffer.update(priceAction, now)
                if (buffer.isAvailable()) {
                    try {
                        if (buyFn.invoke(this, buffer)) results.add(Signal(asset, Rating.BUY, this))
                        if (sellFn.invoke(this, buffer)) results.add(Signal(asset, Rating.SELL, this))
                    } catch (e: InsufficientData) {
                        logger.severe("Not enough data available to calculate the indicators, increase the history size")
                        logger.severe(e.message)
                        throw e
                    }
                }
            }
        }
        return results
    }

    override fun start(phase: Phase) {
        super.start(phase)
        buffers.clear()
    }


    override fun toString() = "TAStrategy $history"

}


fun TALib.recordLow(data: DoubleArray, period: Int) = minIndex(data, period) == data.lastIndex
fun TALib.recordLow(data: PriceBarBuffer, period: Int) = recordLow(data.low, period)

fun TALib.recordHigh(data: DoubleArray, period: Int) = maxIndex(data, period) == data.lastIndex
fun TALib.recordHigh(data: PriceBarBuffer, period: Int) = recordHigh(data.high, period)

fun TALib.vwap(data: PriceBarBuffer, period: Int) : Double {
    val size = data.windowSize
    val c = data.close
    val h = data.high
    val l = data.low
    val v = data.volume
    var sumPrice = 0.0
    var sumVolume = 0.0
    val start = size - period
    if (start < 0) throw InsufficientData("Not sufficient data to calculate vwap")

    for (i in start until size) {
        //Calculate the typical price
        val price = (c[i] + h[i] + l[i]) / 3
        sumPrice += price * v[i]
        sumVolume += v[i]
    }
    return sumPrice / sumVolume
}

class InsufficientData(msg: String) : Exception(msg)