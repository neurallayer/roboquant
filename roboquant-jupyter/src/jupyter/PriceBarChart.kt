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

import org.icepear.echarts.Option
import org.icepear.echarts.charts.bar.BarItemStyle
import org.icepear.echarts.charts.bar.BarSeries
import org.icepear.echarts.charts.candlestick.CandlestickItemStyle
import org.icepear.echarts.charts.candlestick.CandlestickSeries
import org.icepear.echarts.components.coord.AxisLine
import org.icepear.echarts.components.coord.CategoryAxisTick
import org.icepear.echarts.components.coord.SplitArea
import org.icepear.echarts.components.coord.SplitLine
import org.icepear.echarts.components.coord.cartesian.CategoryAxis
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.dataset.Dataset
import org.icepear.echarts.components.grid.Grid
import org.icepear.echarts.components.marker.MarkPoint
import org.icepear.echarts.components.series.Encode
import org.icepear.echarts.components.series.ItemStyle
import org.icepear.echarts.components.title.Title
import org.icepear.echarts.components.tooltip.Tooltip
import org.icepear.echarts.origin.coord.cartesian.AxisOption
import org.icepear.echarts.origin.util.SeriesOption
import org.roboquant.brokers.Trade
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter

/**
 * Plot the price-bars (candlesticks) of an asset found in a [feed] and optionally the [trades] made for that same
 * asset.
 *
 * This will only plot candlesticks if the feed also contains price actions of the type [PriceBar] for the
 * provided [timeframe]. If this is not the case you can use the [PriceChart] instead to plot prices.
 *
 * By default, the chart will use a linear timeline, meaning gaps like a weekend will show-up. This can be disabled
 * by setting [useTime] to false.
 *
 * Besides the prices, you can also provide [trades] to be plotted in the same chart. This will help to visualize when
 * a certain price move triggered a trade.
 */
class PriceBarChart(
    private val feed: Feed,
    private val asset: Asset,
    private val trades: Collection<Trade> = emptyList(),
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val useTime: Boolean = true
) : Chart() {


    init {
        // Default height is not that suitable, so we increase it to 700
        height = 700

        // @TODO once there is support for in the ECharts-Java library, this workaround can be removed
        fix = "option.series[0].encode = { x: 0, y: [1, 4, 3, 2] }; option.series[1].encode = {x: 0, y: 5 };"
    }

    /**
     * Play a feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     */
    private fun fromFeed(): List<List<Any>> {
        val entries = feed.filter<PriceBar>(timeframe) { it.asset == asset }
        val data = entries.map {
            val (now, price) = it
            val direction = if (price.close >= price.open) 1 else -1
            val time = if (useTime) now else now.toString()
            listOf(time, price.open, price.high, price.low, price.close, price.volume, direction)
        }
        return data
    }

    /**
     * Generate mark points that will highlight when a trade happened.
     */
    private fun markPoints(): List<Map<String, Any>> {
        val t = trades.filter { it.asset == asset && timeframe.contains(it.time) }
        val d = mutableListOf<Map<String, Any>>()
        for (trade in t) {
            val time = if (useTime) trade.time else trade.time.toString()
            val price = Amount(asset.currency, trade.price).toBigDecimal()
            val entry = mapOf(
                "value" to trade.size.toBigDecimal(), "xAxis" to time, "yAxis" to price
            )
            d.add(entry)
        }
        return d
    }

    /**
     * Get the series for prices (ohlc) and volume
     */
    private fun getSeries(): Array<SeriesOption> {
        val markPoint = MarkPoint()
            .setData(markPoints().toTypedArray())
            .setItemStyle(ItemStyle().setColor(neutralColor))

        val encode1 = Encode().setItemName(arrayOf("x", "y")).setValue(arrayOf(0, 1))

        val itemStyle1 = CandlestickItemStyle()
            .setColor(positiveColor)
            .setColor0(negativeColor)
            .setBorderColor(positiveColor)
            .setBorderColor0(negativeColor)

        val series1 = CandlestickSeries()
            .setName(asset.symbol)
            .setMarkPoint(markPoint)
            .setEncode(encode1)
            .setItemStyle(itemStyle1)

        val itemStyle2 = BarItemStyle()
            .setColor("#fbe9e")

        val series2 = BarSeries()
            .setXAxisIndex(1)
            .setYAxisIndex(1)
            .setEncode(Encode())
            .setItemStyle(itemStyle2)
            .setLarge(true)

        return arrayOf(series1, series2)
    }

    /**
     * Get the grids for prices and volume
     */
    private fun getGrids(): Array<Grid> {
        return arrayOf(
            Grid().setRight("3%").setLeft(80).setBottom(200),
            Grid().setRight("3%").setLeft(80).setBottom(80).setHeight(80),
        )
    }

    private fun getDataZoom(): Array<DataZoom> {
        return arrayOf(
            DataZoom().setXAxisIndex(arrayOf(0,1)).setType("inside"),
            DataZoom().setXAxisIndex(arrayOf(0,1)).setType("slider")
        )
    }

    private fun getXAxis(): Array<AxisOption> {
        val hide = mapOf("show" to false)
        val noTick = CategoryAxisTick().setShow(false)

        return if (useTime)
            arrayOf(TimeAxis(), TimeAxis().setGridIndex(1).setAxisLabel(hide).setAxisTick(noTick))
        else
            arrayOf(CategoryAxis(), CategoryAxis().setGridIndex(1).setAxisLabel(hide).setAxisTick(noTick))
    }

    private fun getYAxis(): Array<AxisOption> {
        val showSplitArea = SplitArea().setShow(true)
        val hideSplitLine = SplitLine().setShow(false)

        return arrayOf(
            ValueAxis().setScale(true).setSplitArea(showSplitArea),
            ValueAxis()
                .setScale(true)
                .setGridIndex(1)
                .setAxisLine(AxisLine().setShow(false))
                .setAxisLabel(mapOf("show" to false))
                .setSplitLine(hideSplitLine)
        )
    }

    /** @suppress */
    override fun renderOption(): String {

        val line = reduce(fromFeed())
        val timeframe = if (line.size > 1) Timeframe.parse(line.first()[0].toString(), line.last()[0].toString())
            .toString() else ""

        val dataset = Dataset().setSource(line)
        val tooltip = Tooltip().setTrigger("axis")

        val option = Option()
            .setTitle(Title().setText("${asset.symbol} $timeframe"))
            .setGrid(getGrids())
            .setToolbox(getToolbox())
            .setDataset(dataset)
            .setSeries(getSeries())
            .setXAxis(getXAxis())
            .setYAxis(getYAxis())
            .setToolbox(getToolbox())
            .setTooltip(tooltip)
            .setDataZoom(getDataZoom())

        return renderJson(option)
    }

}
