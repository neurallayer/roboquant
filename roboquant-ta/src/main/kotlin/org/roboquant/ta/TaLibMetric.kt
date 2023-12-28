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

package org.roboquant.ta

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.Metric

/**
 * Use a technical indicator from the Ta-Lib library as a metric. Metrics will be available under the
 * name: <metricName>.<symbol> with the symbol name in lowercase.
 *
 * @property initialCapacity the initial number of prices to track.
 * If not enough prices are available to calculate the indicators, the capacity will be automatically increased until
 * it is able to perform the calculations.
 * @property assetFilter which assets to process, default is [AssetFilter.all]
 * @property block the logic to use as a metric
 * @constructor Create new metric
 */
class TaLibMetric(
    private val initialCapacity: Int = 1,
    private val assetFilter: AssetFilter = AssetFilter.all(),
    private var block: TaLib.(series: PriceBarSeries) -> Map<String, Double>
) : Metric {

    private val history = mutableMapOf<Asset, PriceBarSeries>()
    private val taLib = TaLib()

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): Map<String, Double> {
        val metrics = mutableMapOf<String, Double>()
        val actions =
            event.actions.filterIsInstance<PriceBar>().filter { assetFilter.filter(it.asset, event.time) }
        val time = event.time
        for (priceAction in actions) {
            val asset = priceAction.asset
            val buffer = history.getOrPut(asset) { PriceBarSeries(initialCapacity) }
            if (buffer.add(priceAction, time)) {
                try {
                    val postFix = asset.symbol.lowercase()
                    val newMetrics = block.invoke(taLib, buffer).mapKeys { "${it.key}.$postFix" }
                    metrics.putAll(newMetrics)

                } catch (ex: InsufficientData) {
                    buffer.increaseCapacity(ex.minSize)
                }
            }
        }
        return metrics
    }

    /**
     * Reset the state of this metric
     */
    override fun reset() {
        super.reset()
        history.clear()
    }

}
