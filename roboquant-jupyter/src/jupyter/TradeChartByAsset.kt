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
import org.roboquant.common.UnsupportedException

/**
 * Trade chart plots the [trades] that have been generated during a run per Asset. By default, the realized pnl of the
 * trades will be plotted but this is configurable
 *
 */
class TradeChartByAsset(
    private val trades: List<Trade>,
    private val aspect: String = "pnl",
) : Chart() {

    init {
        val validAspects = listOf("pnl", "fee", "cost", "quantity")
        require(aspect in validAspects) { "Unsupported aspect $aspect, valid values are $validAspects" }
    }

    @Suppress("MaxLineLength")
    private fun getTooltip(trade: Trade): String {
        val pnl = trade.pnl.toBigDecimal()
        val totalCost = trade.totalCost.toBigDecimal()
        val fee = trade.fee.toBigDecimal()
        return """
            |asset: ${trade.asset.symbol}<br>
            |currency: ${trade.asset.currency}<br> 
            |time: ${trade.time}<br>
            |qty: ${trade.size}<br>
            |fee: $fee<br>
            |pnl: $pnl<br>
            |cost: $totalCost<br> 
            |order: ${trade.orderId}""".trimMargin()
    }

    private fun toSeriesData(assets: List<Asset>): List<List<Any>> {
        val d = mutableListOf<List<Any>>()
        for (trade in trades.sortedBy { it.time }) {
            with(trade) {
                val value = when (aspect) {
                    "pnl" -> pnl.convert(time = time).toBigDecimal()
                    "fee" -> fee.convert(time = time).toBigDecimal()
                    "cost" -> totalCost.convert(time = time).toBigDecimal()
                    "quantity" -> size.toBigDecimal()
                    else -> throw UnsupportedException("Unsupported aspect $aspect")
                }

                val y = assets.indexOf(asset)
                val tooltip = getTooltip(this)
                d.add(listOf(time, y, value, tooltip))
            }
        }

        return d
    }

    override fun renderOption(): String {
        val assets = trades.map { it.asset }.distinct().sortedBy { it.symbol }
        val d = toSeriesData(assets)

        val series = ScatterSeries()
            .setSymbolSize(10)
            .setData(d)

        val yAxisData = assets.map { it.symbol }.toTypedArray()

        val tooltip = Tooltip()
            .setFormatter(javasciptFunction("return p.value[3];"))

        val chart = Scatter()
            .setTitle("Trade Chart $aspect")
            .addSeries(series)
            .addYAxis(CategoryAxis().setData(yAxisData).setSplitArea(SplitArea().setShow(true)))
            .addXAxis(TimeAxis())
            .setTooltip(tooltip)

        val option = chart.option

        // Allow for both horizontal and vertical zooming
        val toolbox = getToolbox(false)
        val feature = toolbox.feature
        feature["dataZoom"] = ToolboxDataZoomFeature()
        toolbox.feature = feature

        option.setToolbox(toolbox)
        option.setDataZoom(DataZoom())

        return renderJson(option)
    }
}