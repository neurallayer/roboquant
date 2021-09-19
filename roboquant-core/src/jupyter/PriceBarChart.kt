package org.roboquant.jupyter

import org.roboquant.brokers.Trade
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter

/**
 * Plot the price-bars (candlesticks) of an asset and optionally the trades made for that same asset. This will only
 * plot candlesticks if the feed also contains price actions of the type PriceBar. If this is not the case you can use
 * the [PriceChart] instead to plot prices.
 */
class PriceBarChart(
    val feed: Feed,
    val asset: Asset,
    val trades: List<Trade> = listOf(),
    val timeFrame: TimeFrame = TimeFrame.FULL
) : Chart() {


    init {
        height = 700
    }

    /**
     * Play a feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     * @return
     */
    private fun fromFeed(): List<List<Any>> {
        val entries = feed.filter<PriceBar>(timeFrame) { it.asset == asset }
        val data = entries.map {
            val (now, price) = it
            val direction = if (price.close >= price.open) 1 else -1
            listOf(now.toString(), price.open, price.high, price.low, price.close, price.volume, direction)
        }
        return data
    }

    private fun markPoints(): List<Map<String, Any>> {
        val t = trades.filter { it.asset == asset && timeFrame.contains(it.time) }
        val d = mutableListOf<Map<String, Any>>()
        for (trade in t) {
            val entry = mapOf(
                "value" to trade.quantity.toInt(), "xAxis" to trade.time.toString(), "yAxis" to trade.price
            )
            d.add(entry)
        }

        return d
    }


    override fun renderOption(): String {

        val line = fromFeed()
        val lineData = gsonBuilder.create().toJson(line)
        val timeFrame = TimeFrame.parse(line.first()[0].toString(), line.last()[0].toString())

        val marks = markPoints()
        val markData = gsonBuilder.create().toJson(marks)

        return """
                {
                dataset: {
                    source: $lineData
                },
                title: {
                    text: '${asset.symbol} ${timeFrame.toPrettyString()}'
                },
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'line'
                    }
                },
                ${renderToolbox()},
                grid: [
                    {
                        left: 80,
                        right: '3%',
                        bottom: 200
                    },
                    {
                        left: 80,
                        right: '3%',
                        height: 80,
                        bottom: 80
                    }
                ],
                xAxis: [
                    {
                        type: 'category',
                        scale: true,
                        boundaryGap: true,
                        axisLine: {onZero: false},
                        splitLine: {show: false},
                        splitNumber: 20,
                        min: 'dataMin',
                        max: 'dataMax'
                    },
                    {
                        type: 'category',
                        gridIndex: 1,
                        scale: true,
                        boundaryGap: true,
                        axisLine: {onZero: false},
                        axisTick: {show: false},
                        splitLine: {show: false},
                        axisLabel: {show: false},
                        splitNumber: 20,
                        min: 'dataMin',
                        max: 'dataMax'
                    }
                ],
                yAxis: [
                    {
                        scale: true,
                        splitArea: {
                            show: true
                        }
                    },
                    {
                        scale: true,
                        gridIndex: 1,
                        splitNumber: 2,
                        axisLabel: {show: false},
                        axisLine: {show: false},
                        axisTick: {show: false},
                        splitLine: {show: false}
                    }
                ],
                dataZoom: [
                    {
                        type: 'inside',
                        xAxisIndex: [0, 1],
                        start: 10,
                        end: 100
                    },
                    {
                        show: true,
                        xAxisIndex: [0, 1],
                        type: 'slider',
                        bottom: 10,
                        start: 10,
                        end: 100
                    }
                ],
                visualMap: {
                    show: false,
                    seriesIndex: 1,
                    dimension: 6,
                    pieces: [{
                        value: 1,
                        color: 'green'
                    }, {
                        value: -1,
                        color: 'red'
                    }]
                },
                series: [
                    {
                        type: 'candlestick',
                        name: '${asset.symbol}',
                        itemStyle: {
                            color: 'green',
                            color0: 'red',
                            borderColor: 'green',
                            borderColor0: 'red'
                        },
                         markPoint: {
                                data: $markData
                        },
                        encode: {
                            x: 0,
                            y: [1, 4, 3, 2]
                        },
                          
                    },
                    {
                        name: 'Volume',
                        type: 'bar',
                        xAxisIndex: 1,
                        yAxisIndex: 1,
                        itemStyle: {
                            color: '#7fbe9e'
                        },
                        large: true,
                        encode: {
                            x: 0,
                            y: 5
                        }
                    }
                ]
            };
    """.trimStart()
    }

}