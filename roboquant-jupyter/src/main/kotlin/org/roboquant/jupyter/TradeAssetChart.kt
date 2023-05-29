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

package org.roboquant.jupyter

import org.icepear.echarts.Option
import org.icepear.echarts.Scatter
import org.icepear.echarts.charts.scatter.ScatterSeries
import org.icepear.echarts.components.coord.SplitArea
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.toolbox.ToolboxDataZoomFeature
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.brokers.Trade
import org.roboquant.common.Asset
import org.roboquant.common.Currency
import java.math.BigDecimal

/**
 * Trade chart plots the [trades] that have been generated during a run per Asset. By default, the realized pnl of the
 * trades will be used to colour the value, but this is configurable.
 *
 */
class TradeAssetChart(
    private val trades: List<Trade>,
    private val aspect: String = "pnl",
    private val currency: Currency? = null
) : Chart() {

    init {
        val validAspects = listOf("pnl", "fee", "cost", "size")
        require(aspect in validAspects) { "Unsupported aspect $aspect, valid values are $validAspects" }
    }

    private fun toSeriesData(assets: List<Asset>): List<List<Any>> {
        if (trades.isEmpty()) return emptyList()
        val curr = currency ?: trades.first().asset.currency

        val d = mutableListOf<List<Any>>()
        for (trade in trades.sortedBy { it.time }) {
            val value = trade.getValue(aspect, curr)
            val y = assets.indexOf(trade.asset)
            val tooltip = trade.getTooltip()
            d.add(listOf(trade.time, y, value, tooltip))
        }
        return d
    }

    override fun getOption(): Option {
        val assets = trades.map { it.asset }.distinct().sortedBy { it.symbol }
        val d = toSeriesData(assets)

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
            .setTitle(title ?: "Trade Chart $aspect")
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
}