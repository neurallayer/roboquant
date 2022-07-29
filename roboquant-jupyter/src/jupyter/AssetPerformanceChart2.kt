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

package org.roboquant.jupyter

import org.icepear.echarts.Tree
import org.icepear.echarts.charts.tree.TreeSeries
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Use the assets and prices found in the [feed] to plot the assets, their returns and trading volume. in a tree map.
 * This chart works on feeds that contain [price actions][PriceAction] that also have [volume][PriceAction.volume]
 * information
 *
 * If you mix different type of price actions in this feed the result might become less reliable due to the
 * different ways that volume is calculated.
 *
 * @property compensateVolume should the chart compensate the volume by multiplying it with the price or is the
 * volume already expressed in a monetary amount.
 *
 * @TODO once there is support for TreeMap charts in the ECharts-Java library, this implementation can be finsihed.
 */
@Suppress("unused")
class AssetPerformanceChart2(
    private val feed: Feed,
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT",
    private val compensateVolume: Boolean = true
) : Chart() {

    /**
     * Play the feed and get price-actions
     * The output is usable for a treemap
     */
    private fun fromFeed(): List<Map<String, Any>> {
        val result = mutableMapOf<Asset, MutableList<Double>>()  // start, last, volume
        val entries = feed.filter<PriceAction>(timeframe)
        entries.forEach { (time, priceAction) ->
            if (priceAction.volume.isFinite()) {
                val price = priceAction.getPriceAmount(priceType)
                val record = result.getOrPut(priceAction.asset) { mutableListOf(price.value, 0.0, 0.0) }
                record[1] = price.value
                val volume = if (compensateVolume) {
                    price.convert(time = time) * priceAction.volume
                } else {
                    Amount(priceAction.asset.currency, priceAction.volume).convert(time = time)
                }
                record[2] += volume.value
            }
        }
        return result.map {
            val returns = 100.0 * (it.value[1] - it.value[0]) / it.value[0]
            val bdVolume = BigDecimal(it.value[2]).setScale(0, RoundingMode.HALF_DOWN)
            val bdReturns = BigDecimal(returns).setScale(2, RoundingMode.HALF_DOWN)
            mapOf("name" to it.key.symbol, "value" to listOf(bdVolume, bdReturns))
        }
    }

    /** @suppress */
    override fun renderOption(): String {
        val data = fromFeed()
        val max = data.maxOf {
            val x = it["value"] as List<*>
            x[1] as BigDecimal
        }

        val series = TreeSeries()
            .setName("assets")
            .setData(data)

        val tooltip = Tooltip()
            .setPosition("top")
            .setFormatter(javasciptFunction(
                "return 'asset: ' + p.name + '<br>volume: ' + p.value[0]+ '<br>returns: ' + p.value[1]  + '%';"
            ))

        val chart = Tree()
            .addSeries(series)
            .setTitle("Asset Performance")
            .setTooltip(tooltip)
            .setVisualMap(getVisualMap(-max, max))

        val option = chart.option
        option.setToolbox(getBasicToolbox())

        return renderJson(option)
    }


}