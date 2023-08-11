/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.charts

import org.icepear.echarts.Option
import org.icepear.echarts.Treemap
import org.icepear.echarts.charts.treemap.Breadcrumb
import org.icepear.echarts.charts.treemap.TreemapSeries
import org.icepear.echarts.charts.treemap.TreemapSeriesItemStyle
import org.icepear.echarts.charts.treemap.TreemapSeriesLabel
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.*
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Use the assets and prices found in the [feed] to plot the assets, their returns and trading volume as a tree map.
 * This chart works on feeds that contain [price actions][PriceAction] that also have [volume][PriceAction.volume]
 * information
 *
 * If you mix different types of price actions in this feed, the result might become less reliable due to the
 * different ways that volume is calculated.
 *
 * @property compensateVolume compensate the volume by multiplying it with the price. If the volume is already expressed
 * in a monetary amount, set this to false.
 */
class PerformanceChart(
    private val feed: Feed,
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT",
    private val compensateVolume: Boolean = true,
    private val currency: Currency? = null,
    private val assetFilter: AssetFilter = AssetFilter.all()
) : Chart() {

    private class AssetReturns(
        val initialPrice: Double,
        var lastPrice: Double = initialPrice,
        var volume: Double = 0.0
    ) {
        fun returns() = 100.0 * (lastPrice - initialPrice) / initialPrice
    }

    /**
     * Play the feed and get price-actions
     * The output is usable for a treemap
     */
    private fun fromFeed(): List<Map<String, Any>> {
        val result = mutableMapOf<Asset, AssetReturns>()
        val entries = feed.filter<PriceAction>(timeframe)
        val finalEntries = entries.filter { assetFilter.filter(it.second.asset, timeframe.start) }
        if (finalEntries.isEmpty()) return emptyList()

        val curr = currency ?: finalEntries.first().second.asset.currency

        finalEntries.forEach { (time, priceAction) ->
            if (priceAction.volume.isFinite()) {
                val asset = priceAction.asset
                val price = priceAction.getPrice(priceType)
                val record = result.getOrPut(asset) { AssetReturns(price) }
                record.lastPrice = price

                val tradingSize = if (compensateVolume) Size(priceAction.volume) else Size.ONE
                val tradingValue = asset.value(tradingSize, price)
                record.volume += tradingValue.convert(curr, time = time).value
            }
        }
        return result.map {
            val bdVolume = BigDecimal(it.value.volume).setScale(0, RoundingMode.HALF_DOWN)
            val bdReturns = BigDecimal(it.value.returns()).setScale(2, RoundingMode.HALF_DOWN)
            mapOf("name" to it.key.symbol, "value" to listOf(bdVolume, bdReturns))
        }
    }

    /** @suppress */
    override fun getOption(): Option {
        val data = fromFeed()
        val max = data.maxOfOrNull {
            val x = it["value"] as List<*>
            x[1] as BigDecimal
        } ?: BigDecimal.ONE

        val series = TreemapSeries()
            .setName("assets")
            .setData(data)
            .setBreadcrumb(Breadcrumb().setShow(false))
            .setItemStyle(TreemapSeriesItemStyle().setBorderColor("rgba(0,0,0,0)"))
            .setLabel(TreemapSeriesLabel().setFormatter("{b}\n{@[1]}%"))

        val tooltip = Tooltip()
            .setFormatter(
                javascriptFunction(
                    """return 
                        |'symbol: '+p.name+
                        |'<br>trading amount: '+p.value[0].toPrecision(4)+ 
                        |'<br>returns: '+p.value[1]  + '%';""".trimMargin().replace("\n", "")
                )
            )

        val vm = getVisualMap(-max, max)
        vm.color = arrayOf(positiveColor, negativeColor)

        val chart = Treemap()
            .addSeries(series)
            .setTitle(title ?: "Asset Performance")
            .setTooltip(tooltip)
            .setVisualMap(vm)

        val option = chart.option
        option.setToolbox(getBasicToolbox())

        return option
    }

}