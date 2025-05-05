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

package org.roboquant.strategies

import org.roboquant.common.Asset
import org.roboquant.common.Event
import org.roboquant.common.Signal

/**
 * SignalStrategy that uses the crossover of two Exponential Moving Averages (EMA) to generate a BUY or SELL signal. It is
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
 * @constructor Create a new EMACrossover strategy
 *
 * @param fastPeriod The shorter (fast) period or fast EMA in number of events, default is 12
 * @param slowPeriod The longer (slow) period or slow EMA in number of events, default is 26
 * @param smoothing The smoothing factor to use, default is 2.0
 * @property minEvents minimal number of events observed before starting to execute the strategy, default is the same
 * @property priceType the type of price to use, like "CLOSE" or "OPEN", default is "DEFAULT"
 * as the slow period
 */
class EMACrossover(
    fastPeriod: Int = 12,
    slowPeriod: Int = 26,
    smoothing: Double = 2.0,
    private val minEvents: Int = slowPeriod,
    private val priceType: String = "DEFAULT"
) : Strategy {

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
         * @return new EMACrossover
         */
        val PERIODS_50_200: EMACrossover
            get() = EMACrossover(50, 200)

        /**
         * Predefined EMA Crossover with 12 steps for fast EMA and 26 steps for slow EMA
         *
         * @return new EMACrossover
         */
        val PERIODS_12_26: EMACrossover
            get() = EMACrossover(12, 26)

        /**
         * Predefined EMA Crossover with 5 steps for fast EMA and 15 steps for slow EMA
         *
         * @return new EMACrossover
         */
        val PERIODS_5_15: EMACrossover
            get() = EMACrossover(5, 15)
    }

    private inner class EMACalculator(initialPrice: Double) {
        private var step = 1L
        var emaFast = initialPrice
        var emaSlow = initialPrice

        fun addPrice(price: Double): Boolean {
            emaFast = emaFast * fast + (1 - fast) * price
            emaSlow = emaSlow * slow + (1 - slow) * price
            step += 1
            return step >= minEvents
        }

        fun getDirection() = if (emaFast > emaSlow) 1.0 else -1.0
    }

    override fun createSignals(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()
        for ((asset, priceItem) in event.prices) {
            val price = priceItem.getPrice(priceType)
            val signal = generate(asset, price)
            if (signal != null) signals.add(signal)
        }
        return signals
    }

    private fun generate(asset: Asset, price: Double): Signal? {
        val calculator = calculators[asset]
        if (calculator == null) {
            calculators[asset] = EMACalculator(price)
            return null
        }

        val oldDirection = calculator.getDirection()
        calculator.addPrice(price)
        if (calculator.addPrice(price)) {
            val newDirection = calculator.getDirection()

            if (oldDirection != newDirection) {
                return Signal(asset, newDirection)
            }
        }

        return null
    }


}
