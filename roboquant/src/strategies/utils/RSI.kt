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

package org.roboquant.strategies.utils

/**
 * Relative Strength Index implementation with a small twist. It uses exponential moving average for all
 * calculations, so also for calculating the first value (and not the often used unweighted average)
 *
 * @constructor Create new RSI calculator
 */
class RSI(initialPrice: Double, private val periods: Int = 14, private val minSteps: Int = periods) {

    private var avgGain = Double.NaN
    private var avgLoss = Double.NaN
    private var lastPrice: Double = initialPrice
    private var steps = 0L

    /**
     * Add a new price
     *
     * @param price
     */
    fun add(price: Double) {
        steps++
        val gain = 1.0 - price / lastPrice
        lastPrice = price
        if (gain > 0.0) {
            avgGain = if (avgGain.isNaN()) gain else update(avgGain, gain)
        } else if (gain < 0.0) {
            avgLoss = if (avgLoss.isNaN()) gain else update(avgLoss, gain)
        }
    }

    private fun update(old: Double, update: Double) = (old * (periods - 1) + update) / periods

    /**
     * Is there enough data to calculate the RSI
     *
     */
    fun isReady() = steps >= minSteps

    /**
     * Calculate the RSI value
     *
     * @return
     */
    fun calculate(): Double {

        return if ((!avgGain.isNaN()) && (!avgLoss.isNaN()))
            100.0 - (100.0 / (1.0 + (avgGain / -avgLoss)))
        else if (avgGain.isNaN() && avgLoss.isNaN())
            50.0
        else if (avgLoss.isNaN())
            100.0
        else
            0.0
    }


}