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

package org.roboquant.strategies.utils

import org.roboquant.feeds.PriceBar

/**
 * Calculate the VWAP for a series of [PriceBar] based on a fixed moving window. This can be used to track VWAP over
 * a longer period of time.
 *
 * @constructor Create new VWAP calculator
 */
class VWAPCalculator(windowSize: Int = 100) {

    val total = MovingWindow(windowSize)
    val volume = MovingWindow(windowSize)

    fun add(action: PriceBar) {
        val v = action.volume
        total.add(action.getPrice("TYPICAL") * v)
        volume.add(v)
    }

    fun isReady() = total.isAvailable()

    fun calc(): Double {
        return total.toDoubleArray().sum() / volume.toDoubleArray().sum()
    }

    fun clear() {
        total.clear()
        volume.clear()
    }


}