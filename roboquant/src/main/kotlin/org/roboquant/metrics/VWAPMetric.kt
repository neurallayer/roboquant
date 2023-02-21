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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Calculate the daily VWAP (Volume Weighted Average Price) for the assets in a feed.
 *
 * Please note VWAP is a single-day indicator, as soon as the metric detects that a new trading day started, the VWAP
 * will be reset. Also, it requires [PriceBar] data to be fully accurate, since it needs the "TYPICAL" price.
 *
 *      VWAP = Cumulative Typical Price x Volume/Cumulative Volume
 *
 * @property minSize minimum number of events before calculating the first VWAP, default is 2
 * @constructor Create new instance of the VWAPMetric
 */
class VWAPMetric(val minSize: Int = 2) : Metric {

    private val calculators = mutableMapOf<Asset, VWAPDaily>()

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Double>()
        for (priceBar in event.prices.values.filterIsInstance<PriceBar>()) {
            val calc = calculators.getOrPut(priceBar.asset) { VWAPDaily(minSize) }
            calc.add(priceBar, event.time)
            if (calc.isReady()) {
                result["vwap.${priceBar.asset.symbol}"] = calc.calc()
            }
        }
        return result
    }

    override fun reset() {
        calculators.clear()
    }
}

/**
 * Calculates the Daily VWAP. VWAP is a single-day indicator, and is restarted at the start of each new trading day.
 * It uses the exchange of the underlying asset to determine what is still within one day.
 *
 * @property minSteps The minimum number of steps required
 * @constructor Create empty Daily VWAP calculator
 */
private class VWAPDaily(private val minSteps: Int) {

    private val total = mutableListOf<Double>()
    private val volume = mutableListOf<Double>()
    private var last: Instant = Instant.MIN

    fun add(action: PriceBar, time: Instant) {
        val exchange = action.asset.exchange
        if (last != Instant.MIN && !exchange.sameDay(time, last)) clear()
        last = time
        val v = action.volume
        total.add(action.getPrice("TYPICAL") * v)
        volume.add(v)
    }

    fun isReady() = total.size >= minSteps

    fun calc(): Double {
        return total.sum() / volume.sum()
    }

    private fun clear() {
        total.clear()
        volume.clear()
        last = Instant.MIN
    }
}

