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

package org.roboquant.strategies

import org.roboquant.common.Asset
import java.time.Instant

/**
 * Strategy that uses the crossover of two Exponential Moving Averages (EMA) to generate a BUY or SELL signal. It is
 * frequently used in FOREX trading, but can also be applied to other asset classes.
 *
 * The rules are straight forward:
 *
 * - If the fast EMA trend crosses over the slow EMA trend, generate a BUY signal
 * - If the fast EMA trend crosses under the slow EMA trend, generate a SELL signal
 * - Don't generate a signal in all other scenarios
 *
 * This is a computational and memory efficient implementation since it doesn't store historic prices in memory.
 *
 * @constructor Create a new EMAStrategy strategy
 *
 * @param fastPeriod The shorter (fast) period or fast EMA in number of events, default is 12
 * @param slowPeriod The longer (slow) period or slow EMA in number of events, default is 26
 * @param smoothing The smoothing factor to use, default is 2.0
 * @property minEvents minimal number of events observed before starting to execute the strategy, default is the same
 * @property priceType the type of price to use, like "CLOSE" or "OPEN", default is "DEFAULT"
 * as the slow period
 */
class EMAStrategy(
    fastPeriod: Int = 12,
    slowPeriod: Int = 26,
    smoothing: Double = 2.0,
    private val minEvents: Int = slowPeriod,
    priceType: String = "DEFAULT"
) : PriceStrategy(priceType = priceType, prefix = "strategy.ema.") {

    private val fast = 1.0 - (smoothing / (fastPeriod + 1))
    private val slow = 1.0 - (smoothing / (slowPeriod + 1))
    private val calculators = HashMap<Asset, EMACalculator>()

    /**
     * Standard set of predefined EMA Strategies that are commonly used in the industry
     */
    companion object Factory {
        /**
         * Predefined EMA Crossover with 50 steps for the fast trend and 200 steps for slow trend
         *
         * @return new EMAStrategy
         */
        val PERIODS_50_200: EMAStrategy
            get() = EMAStrategy(50, 200)

        /**
         * Predefined EMA Crossover with 12 steps for fast EMA and 26 steps for slow EMA
         *
         * @return new EMAStrategy
         */
        val PERIODS_12_26: EMAStrategy
            get() = EMAStrategy(12, 26)

        /**
         * Predefined EMA Crossover with 5 steps for fast EMA and 15 steps for slow EMA
         *
         * @return new EMAStrategy
         */
        val PERIODS_5_15: EMAStrategy
            get() = EMAStrategy(5, 15)
    }

    private inner class EMACalculator(initialPrice: Double) {
        private var step = 1L
        var emaFast = initialPrice
        var emaSlow = initialPrice

        fun addPrice(price: Double) {
            emaFast = emaFast * fast + (1 - fast) * price
            emaSlow = emaSlow * slow + (1 - slow) * price
            step += 1
        }

        fun isReady(): Boolean = step >= minEvents

        fun getDirection(): Boolean = emaFast > emaSlow
    }

    override fun generate(asset: Asset, price: Double, time: Instant): Signal? {
        val calculator = calculators[asset]
        if (calculator != null) {
            val oldDirection = calculator.getDirection()
            calculator.addPrice(price)
            if (calculator.isReady()) {
                val newDirection = calculator.getDirection()

                if (recording) {
                    record("${asset.symbol}.fast", calculator.emaFast)
                    record("${asset.symbol}.slow", calculator.emaSlow)
                }

                if (oldDirection != newDirection) {
                    val rating = if (newDirection) Rating.BUY else Rating.SELL
                    return Signal(asset, rating)
                }
            }
        } else {
            calculators[asset] = EMACalculator(price)
        }

        return null
    }

    override fun reset() {
        calculators.clear()
    }

}