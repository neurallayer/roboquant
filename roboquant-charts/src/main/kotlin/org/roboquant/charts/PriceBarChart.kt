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

import org.icepear.echarts.Option
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
import org.icepear.echarts.components.series.Encode
import org.icepear.echarts.components.title.Title
import org.icepear.echarts.components.tooltip.Tooltip
import org.icepear.echarts.components.visualMap.PiecewiseVisualMap
import org.icepear.echarts.components.visualMap.VisualPiece
import org.icepear.echarts.origin.coord.cartesian.AxisOption
import org.icepear.echarts.origin.util.SeriesOption
import org.roboquant.common.Asset
import org.roboquant.common.Stock
import org.roboquant.common.Timeframe
import org.roboquant.common.getBySymbol
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter
import org.roboquant.metrics.Indicator

/**
 * Plot the price-bars (candlesticks) of an [asset] found in a [feed].
 *
 * This chart will only plot candlesticks if the feed also contains price actions of the type [PriceBar] for the
 * provided [timeframe]. If this is not the case, you can use the [PriceChart] instead to plot the prices.
 *
 * By default, the chart will use a linear timeline, meaning gaps like a weekend will show-up. This can be disabled
 * by setting [useTime] to false.
 */
class PriceBarChart(
    private val feed: Feed,
    private val asset: Asset,
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val useTime: Boolean = true,
    private vararg val indicators: Indicator
) : Chart() {

    /**
     * Plot the price-bars of a [symbol] found in a [feed] and optionally the [trades] made for that same
     * asset.
     *
     * @see PriceBarChart
     */
    constructor(
        feed: AssetFeed,
        symbol: String,
        timeframe: Timeframe = Timeframe.INFINITE,
        useTime: Boolean = true
    ) : this(feed, feed.assets.getBySymbol(symbol), timeframe, useTime)


    /**
     * Plot the price-bars of a [symbol] found in a [feed] and optionally the [trades] made for that same
     * asset.
     *
     * @see PriceBarChart
     */
    constructor(
        feed: Feed,
        symbol: String,
        timeframe: Timeframe = Timeframe.INFINITE,
        useTime: Boolean = true
    ) : this(feed, Stock(symbol), timeframe, useTime)


    init {
        // Default height is not that suitable for this chart type, so we increase it to 700
        height = 700
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
            val volume = if (price.volume.isFinite()) price.volume else 0.0
            listOf(time, price.open, price.high, price.low, price.close, volume, direction)
        }
        return data
    }



    /**
     * Get the series for prices (ohlc) and volume
     */
    private fun getSeries(): List<SeriesOption> {


        val encode1 = Encode().setX(0).setY(arrayOf(1, 4, 3, 2))

        val itemStyle1 = CandlestickItemStyle()
            .setColor(positiveColor)
            .setColor0(negativeColor)
            .setBorderColor(positiveColor)
            .setBorderColor0(negativeColor)

        val series1 = CandlestickSeries()
            .setName(asset.symbol)
            .setEncode(encode1)
            .setItemStyle(itemStyle1)

        val encode2 = Encode().setX(0).setY(5)

        val series2 = BarSeries()
            .setXAxisIndex(1)
            .setYAxisIndex(1)
            .setEncode(encode2)
            .setLarge(true)
            .setColor("#7fbe9e")

        val series3 = indicators.toLineSeries(feed, asset, timeframe)

        return listOf(series1, series2) + series3
    }

    private fun getVM(): PiecewiseVisualMap {
        return PiecewiseVisualMap()
            .setShow(false)
            .setSeriesIndex(1)
            .setDimension(6)
            .setPieces(
                arrayOf(
                    VisualPiece().setValue(1).setColor(positiveColor),
                    VisualPiece().setValue(-1).setColor(negativeColor)
                )
            )
    }

    /**
     * Get the grids for candlestick and volume areas
     */
    private fun getGrids(): Array<Grid> {
        return arrayOf(
            Grid().setRight("3%").setLeft(80).setBottom(200),
            Grid().setRight("3%").setLeft(80).setBottom(80).setHeight(80),
        )
    }

    /**
     * Get data zoom ensuring both candlestick and volume zoom at the same time
     */
    private fun getDataZoom(): Array<DataZoom> {
        return arrayOf(
            DataZoom().setXAxisIndex(arrayOf(0, 1)).setType("inside"),
            DataZoom().setXAxisIndex(arrayOf(0, 1)).setType("slider")
        )
    }

    private fun getXAxis(): Array<AxisOption> {
        val hide = mapOf("show" to false)
        val noTick = CategoryAxisTick().setShow(false)

        return if (useTime)
            arrayOf(
                TimeAxis(),
                TimeAxis().setGridIndex(1).setAxisLabel(hide).setAxisTick(noTick)
            )
        else
            arrayOf(
                CategoryAxis(),
                CategoryAxis().setGridIndex(1).setAxisLabel(hide).setAxisTick(noTick)
            )
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
    override fun getOption(): Option {
        val line = reduce(fromFeed())
        val dataset = Dataset().setSource(line)
        val tooltip = Tooltip().setTrigger("axis")

        return Option()
            .setTitle(Title().setText(title ?: asset.symbol))
            .setGrid(getGrids())
            .setDataset(dataset)
            .setSeries(getSeries().toTypedArray())
            .setXAxis(getXAxis())
            .setYAxis(getYAxis())
            .setToolbox(getToolbox())
            .setTooltip(tooltip)
            .setDataZoom(getDataZoom())
            .setVisualMap(getVM())
    }

}
