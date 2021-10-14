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
import java.time.Instant

import java.time.temporal.ChronoUnit

/**
 * Calculates the Daily VWAP. VWAP is a single-day indicator, and is restarted at the opening of each new trading day.
 * If there is more than 8 hours between two events, it is considered to be a new day and the previous calculations
 * are reset.
 *
 * @property minSteps The minimum number of steps required
 * @constructor Create empty Daily VWAP calculator
 */
class VWAPDaily(private val minSteps: Int = 1) {

    private val total = mutableListOf<Double>()
    private val volume = mutableListOf<Double>()
    private var last: Instant = Instant.MIN


    fun add(action: PriceBar, now: Instant) {
        if (now > last.plus(8, ChronoUnit.HOURS)) clear()
        last = now
        val v = action.volume.toDouble()
        total.add(action.getPrice("TYPICAL") * v)
        volume.add(v)
    }

    fun isReady() = total.size >= minSteps

    fun calc(): Double {
        return total.sum() / volume.sum()
    }

    fun clear() {
        total.clear()
        volume.clear()
        last = Instant.MIN
    }
}