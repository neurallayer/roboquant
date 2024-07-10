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

package org.roboquant.charts

import org.icepear.echarts.Line
import org.icepear.echarts.Option
import org.icepear.echarts.charts.line.LineAreaStyle
import org.icepear.echarts.charts.line.LineSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.legend.Legend
import org.icepear.echarts.components.marker.MarkPoint
import org.icepear.echarts.components.series.ItemStyle
import org.icepear.echarts.components.series.LineStyle
import org.roboquant.brokers.sim.Trade
import org.roboquant.common.*
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.filter
import org.roboquant.metrics.Indicator
import org.roboquant.metrics.apply

internal fun Array<out Indicator>.toLineSeries(feed: Feed, asset: Asset, timeframe: Timeframe): List<LineSeries> {
    val data = mutableMapOf<String, TimeSeries>()
    for (indicator in this) {
        val map = feed.apply(indicator, asset, timeframe = timeframe, addSymbolPostfix = false)
        data.putAll(map)
    }
    val result = mutableListOf<LineSeries>()
    val currency = asset.currency
    for ((key, timeseries) in data) {
        val values = timeseries.map { Pair(it.time, Amount(currency, it.value).toBigDecimal()) }
        val lineSeries = LineSeries()
            .setData(values)
            .setName(key)
            .setShowSymbol(false)
            .setLineStyle(LineStyle().setWidth(1))
        result.add(lineSeries)
    }
    return result
}

/**
 * Plot the prices of an [asset] found in the [feed] and optionally the [trades] made for that same asset. When
 * supplying trades, the corresponding [Trade.size] will we plotted as markers.
 *
 * This chart supports any type of PriceItem.
 *
 * If you want to plot a candlestick chart, use the [PriceBarChart] instead.
 */
class PriceChart(
    private val feed: Feed,
    private val asset: Asset,
    private val trades: Collection<Trade> = emptyList(),
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT",
    private vararg val indicators: Indicator
) : Chart() {

    /**
     * Plot the prices of a [symbol] found in a [feed] and optionally the [trades] made for that same
     * asset.
     *
     * @see PriceChart
     */
    constructor(
        feed: AssetFeed,
        symbol: String,
        trades: Collection<Trade> = emptyList(),
        timeframe: Timeframe = Timeframe.INFINITE,
        priceType: String = "DEFAULT"
    ) : this(feed, feed.assets.getBySymbol(symbol), trades, timeframe, priceType)

    /**
     * Play the feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     */
    private fun priceSeries(): LineSeries {
        val entries = feed.filter<PriceItem>(timeframe) { it.asset == asset }
        val data = entries.map {
            // val price = it.second.getPriceAmount(priceType)
            // it.first to price.toBigDecimal()
            val price = it.second.getPrice(priceType)
            it.first to price
        }

        return LineSeries()
            .setData(reduce(data))
            .setName("price")
            .setShowSymbol(false)
            .setLineStyle(LineStyle().setWidth(1))
            .setAreaStyle(LineAreaStyle().setOpacity(0.1))
    }

    /**
     * Generate mark points that will highlight when a trade happened.
     */
    private fun markPointsData(): Array<Map<String, Any>> {
        val t = trades.filter { it.asset == asset && timeframe.contains(it.time) }
        val result = mutableListOf<Map<String, Any>>()
        for (trade in t) {
            val entry = mapOf(
                "value" to trade.size.toBigDecimal(),
                "xAxis" to trade.time,
                "yAxis" to trade.price
            )
            result.add(entry)
        }
        return result.toTypedArray()
    }

    /** @suppress */
    override fun getOption(): Option {

        val priceLineSeries = priceSeries()

        val mpData = markPointsData()
        if (mpData.isNotEmpty()) priceLineSeries.markPoint = MarkPoint()
            .setData(mpData)
            .setItemStyle(ItemStyle().setColor(neutralColor))

        val xAxis = TimeAxis()
        val yAxis = ValueAxis().setScale(true)

        val chart = Line()
            .setTitle(title ?: asset.symbol)
            .addSeries(priceLineSeries)
            .addXAxis(xAxis)
            .addYAxis(yAxis)
            .setTooltip("axis")

        val indicatorLiceSeries = indicators.toLineSeries(feed, asset, timeframe)
        for (s in indicatorLiceSeries) chart.addSeries(s)
        if (indicatorLiceSeries.isNotEmpty()) {
            val legendData = indicatorLiceSeries.map { it.name }
            chart.setLegend(Legend().setData(legendData.toTypedArray()))
        }

        val option = chart.option
        option.backgroundColor = "rgba(0,0,0,0)"
        option.setToolbox(getToolbox())
        option.setDataZoom(DataZoom())

        return option
    }

}
