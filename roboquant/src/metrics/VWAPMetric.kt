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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.strategies.utils.VWAPDaily

/**
 * Calculate the daily VWAP (Volume Weighted Average Price) for all the assets in a feed
 *
 * @property minSize minimum number of events before calculating the first VWAP
 * @constructor Create new VWAP metric
 */
class VWAPMetric(val minSize: Int = 2) : SimpleMetric() {

    private val calculators = mutableMapOf<Asset, VWAPDaily>()

    /**
     * Calculate the metric given the account and step. This method will be invoked at the
     * end of each step in a run. The result is [Map] with keys being a unique metric name and the value
     * the calculated metric value. If no metrics are calculated, an empty map should be returned instead.
     *
     * Please note VWAP is a single-day indicator, so it only makes sense in the context of intraday events.
     *
     * @param account
     * @return
     */
    override fun calc(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Double>()
        for (priceBar in event.prices.values.filterIsInstance<PriceBar>()){
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