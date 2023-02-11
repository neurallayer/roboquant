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

package org.roboquant.jupyter

import org.icepear.echarts.Line
import org.icepear.echarts.Option
import org.icepear.echarts.charts.line.LineSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.marker.MarkPoint
import org.icepear.echarts.components.series.ItemStyle
import org.icepear.echarts.components.series.LineStyle
import org.roboquant.brokers.Trade
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.time.Instant

/**
 * Plot the prices of an [asset] found in the [feed] and optionally the [trades] made for that same asset. This
 * chart supports any type of PriceAction.
 *
 * If you want to plot a candlestick chart, use the [PriceBarChart] instead.
 */
class PriceChart(
    private val feed: Feed,
    private val asset: Asset,
    private val trades: Collection<Trade> = emptyList(),
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT"
) : Chart() {

    /**
     * Play the feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     */
    private fun fromFeed(): List<Pair<Instant, Double>> {
        val entries = feed.filter<PriceAction>(timeframe) { it.asset == asset }
        val data = entries.map {
            // val price = it.second.getPriceAmount(priceType)
            // it.first to price.toBigDecimal()
            val price = it.second.getPrice(priceType)
            it.first to price
        }
        return data
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
                "yAxis" to trade.price // Amount(trade.asset.currency, trade.price).toBigDecimal()
            )
            result.add(entry)
        }
        return result.toTypedArray()
    }

    /** @suppress */
    override fun getOption(): Option {
        val line = reduce(fromFeed())
        val timeframe = if (line.size > 1) Timeframe(line.first().first, line.last().first).toString() else ""

        val lineSeries = LineSeries()
            .setData(line)
            .setShowSymbol(false)
            .setLineStyle(LineStyle().setWidth(1))

        val mpData = markPointsData()
        if (mpData.isNotEmpty()) lineSeries.markPoint = MarkPoint()
            .setData(mpData)
            .setItemStyle(ItemStyle().setColor(neutralColor))

        val xAxis = TimeAxis()
        val yAxis = ValueAxis().setScale(true)

        val chart = Line()
            .setTitle(title ?: "${asset.symbol} $timeframe")
            .addSeries(lineSeries)
            .addXAxis(xAxis)
            .addYAxis(yAxis)
            .setTooltip("axis")

        val option = chart.option
        option.backgroundColor = "rgba(0,0,0,0)"
        option.setToolbox(getToolbox())
        option.setDataZoom(DataZoom())

        return option
    }

}