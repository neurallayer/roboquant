/*
 * Copyright 2022 Neural Layer
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

import org.roboquant.feeds.PriceBar
import kotlin.math.abs

/**
 * Efficient Average True Range implementation that minmizes the memory footprint and uses an exponential moving average
 * to calculate the average over a [period].
 *
 * @constructor Create new ATR
 */
class ATR(private val period: Int = 5) {

    private var prevClose = Double.NaN
    private var value = Double.NaN
    private var n = 0L

    fun add(price: PriceBar) {
        if (!prevClose.isNaN()) {
            val tr = maxOf(price.high - price.low, abs(price.high - prevClose), abs(price.low - prevClose))
            value = if (value.isNaN()) tr else (value * (period - 1) + tr) / period
        }
        prevClose = price.close
        n++
    }

    fun isReady() = n >= period

    fun calc() = value

    fun reset() {
        prevClose = Double.NaN
        value = Double.NaN
        n = 0L
    }

}