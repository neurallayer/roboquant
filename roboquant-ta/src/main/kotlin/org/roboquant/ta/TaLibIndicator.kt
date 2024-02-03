/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.ta

import org.roboquant.feeds.Action
import org.roboquant.metrics.Indicator
import java.time.Instant

/**
 * This class enables the creation of an [Indicator] based on TaLib.
 *
 * Example:
 * ```
 * val indicator = TaLibIndicator() {
 *      mapOf("ema" to ema(it, 20))
 * }
 * ```
 * @param initialCapacity the initial number of prices to track, default is one.
 * If not enough prices are available to calculate an indicator, the capacity will be automatically increased until
 * it is able to perform the calculations.
 * @property block the function that should return a map containing the indicator values.
 */
class TaLibIndicator(
    initialCapacity: Int = 1,
    private val block: TaLib.(series: PriceBarSeries) -> Map<String, Double>
) : Indicator {

    private val taLib = TaLib()
    private val series = PriceBarSeries(initialCapacity)

    /**
     * @see Indicator.calculate
     */
    override fun calculate(action: Action, time: Instant): Map<String, Double> {
        if (series.add(action, time)) {
            try {
                return block.invoke(taLib, series)
            } catch (ex: InsufficientData) {
                // Increase the capacity if we don't have enough data yet
                series.increaseCapacity(ex.minSize)
            }
        }
        return emptyMap()
    }

    /**
     * @see Indicator.clear
     */
    override fun clear() {
        series.clear()
    }

    /**
     * Commonly used indicators using the TaLib library
     */
    companion object {

        /**
         * Return a `Relative Strength Indicator` for the provided [barCount]
         */
        fun rsi(barCount: Int = 10): TaLibIndicator {
            return TaLibIndicator {
                mapOf("rsi$barCount" to rsi(it, barCount))
            }
        }

        /**
         * Return a `Bollinger Bands` Indicator for the provided [barCount]
         */
        fun bbands(barCount: Int = 10): TaLibIndicator {
            return TaLibIndicator {
                val (high, mid, low) = bbands(it, barCount)
                val prefix = "bb$barCount"
                mapOf("$prefix.low" to low, "$prefix.high" to high, "$prefix.mid" to mid)
            }
        }

        /**
         * Return an `Exponential Moving Average` Indicator for the provided [barCount]
         */
        fun ema(barCount: Int = 10): TaLibIndicator {
            return TaLibIndicator {
                mapOf("ema$barCount" to ema(it, barCount))
            }
        }

        /**
         * Return a `Simple Moving Average` Indicator for the provided [barCount]
         */
        fun sma(barCount: Int = 10): TaLibIndicator {
            return TaLibIndicator {
                mapOf("sma$barCount" to sma(it, barCount))
            }
        }

        /**
         * Return a `Money Flow In` Indicator for the provided [barCount]
         */
        fun mfi(barCount: Int = 10): TaLibIndicator {
            return TaLibIndicator {
                mapOf("mfi$barCount" to mfi(it, barCount))
            }
        }

        /**
         * Return a `Stochastic` Indicator for the provided periods
         */
        fun stochastic(fastKPeriod: Int = 5, slowKPeriod: Int = 3, slowDPeriod: Int = slowKPeriod): TaLibIndicator {
            return TaLibIndicator {
                val (d, k) = stoch(it, fastKPeriod, slowKPeriod, slowDPeriod = slowDPeriod)
                mapOf("stochatic.d" to d, "stochatic.k" to k)
            }
        }

    }

}

