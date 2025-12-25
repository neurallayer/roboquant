/*
 * Copyright 2020-2026 Neural Layer
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
import org.icepear.echarts.Scatter
import org.icepear.echarts.charts.scatter.ScatterSeries
import org.icepear.echarts.components.coord.SplitArea
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.toolbox.ToolboxDataZoomFeature
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Currency
import org.roboquant.common.Trade
import java.math.BigDecimal
import java.time.Instant


internal fun Trade.getTooltip(): String {
    val pnl = pnl.toBigDecimal()
    return """
            |symbol: ${asset.symbol}<br>
            |currency: ${asset.currency}<br>
            |time: $time<br>
            |size: $size<br>
            |pnl: $pnl<br>""".trimMargin()
}


/**
 * Trade chart plots the trades of an [trades] that have been generated during a run.
 */
class TradeChart(
    private val trades: List<Trade>,
    private val currency: Currency? = null,
    private val perAsset: Boolean = false
) : Chart() {


    private fun tradesToSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        if (trades.isEmpty()) return emptyList()
        val curr = currency ?: trades.first().asset.currency
        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (trade in trades.sortedBy { it.time }) {
            val value = Amount(trade.asset.currency, trade.pnl).convert(curr, trade.time).toBigDecimal()
            val tooltip = trade.getTooltip()
            d.add(Triple(trade.time, value, tooltip))
        }
        return d
    }

    private fun getOptionNormal(): Option {

        val data = tradesToSeriesData()
        val max = data.maxOfOrNull { it.second }
        val min = data.minOfOrNull { it.second }

        val series = ScatterSeries()
            .setData(data)
            .setSymbolSize(10)

        val vm = getVisualMap(min, max).setDimension(1)

        val tooltip = Tooltip()
            .setFormatter(javascriptFunction("return p.value[2];"))

        val chart = Scatter()
            .setTitle(title ?: "Trade Chart")
            .addXAxis(TimeAxis())
            .addYAxis(ValueAxis().setScale(true))
            .addSeries(series)
            .setVisualMap(vm)
            .setTooltip(tooltip)

        val option = chart.option
        option.setToolbox(getToolbox(includeMagicType = false))
        option.setDataZoom(DataZoom())

        return option
    }

    private fun toSeriesData2(assets: List<Asset>): List<List<Any>> {
        if (trades.isEmpty()) return emptyList()
        val curr = currency ?: trades.first().asset.currency

        val d = mutableListOf<List<Any>>()
        for (trade in trades.sortedBy { it.time }) {
            val value = Amount(trade.asset.currency, trade.pnl).convert(curr, trade.time).toBigDecimal()
            val y = assets.indexOf(trade.asset)
            val tooltip = trade.getTooltip()
            d.add(listOf(trade.time, y, value, tooltip))
        }
        return d
    }

    private fun getOptionPerAsset(): Option {
        val assets = trades.map { it.asset }.distinct().sortedBy { it.symbol }
        val d = toSeriesData2(assets)

        val series = ScatterSeries()
            .setSymbolSize(10)
            .setData(d)

        val yAxisData = assets.map { it.symbol }.toTypedArray()

        val tooltip = Tooltip()
            .setFormatter(javascriptFunction("return p.value[3];"))

        val valueDim = 2
        val min = d.minOfOrNull { it[valueDim] as BigDecimal }
        val max = d.maxOfOrNull { it[valueDim] as BigDecimal }
        val vm = getVisualMap(min, max).setDimension(valueDim)

        val chart = Scatter()
            .setTitle(title ?: "Trade Chart Chart")
            .addSeries(series)
            .addYAxis(CategoryAxis().setData(yAxisData).setSplitArea(SplitArea().setShow(true)))
            .addXAxis(TimeAxis())
            .setTooltip(tooltip)
            .setVisualMap(vm)

        val option = chart.option

        // Allow for both horizontal and vertical zooming
        val toolbox = getToolbox(false)
        val feature = toolbox.feature
        feature["dataZoom"] = ToolboxDataZoomFeature()
        toolbox.feature = feature
        option.setToolbox(toolbox)
        option.setDataZoom(DataZoom())

        return option
    }

    override fun getOption(): Option {
        return if (perAsset) getOptionPerAsset() else getOptionNormal()
    }


}
