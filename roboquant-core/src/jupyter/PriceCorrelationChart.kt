package org.roboquant.jupyter

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.common.clean
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Shows the correlation matrix between the prices of two or more assets
 */
class PriceCorrelationChart(
    private val feed: Feed,
    private val assets: Collection<Asset>,
    private val timeFrame: TimeFrame = TimeFrame.FULL,
    private val priceType: String = "DEFAULT",
    private val scale:Int = 2,
    private val minObservations: Int = 3
) : Chart() {

    init {
        require(assets.size > 1) { "Minimum of 2 assets are required, found ${assets.size}" }
    }

    private fun collectPrices(): Map<Asset, List<Double>> = runBlocking {
        val channel = EventChannel(timeFrame = timeFrame)
        val result = TreeMap<Asset, MutableList<Double>>()

        val job = launch {
            feed.play(channel)
            channel.close()
        }

        try {
            while (true) {
                val o = channel.receive()
                for (asset in assets) {
                    val price = o.getPrice(asset, priceType) ?: Double.NaN
                    val list = result.getOrPut(asset) { mutableListOf()}
                    list.add(price)
                }
            }

        } catch (e: ClosedReceiveChannelException) {

        } finally {
            channel.close()
            if (job.isActive) job.cancel()
        }
        return@runBlocking result
    }

    private fun getMatrix(prices: Map<Asset, List<Double>>): MutableList<Triple<Int, Int, BigDecimal>> {
        val calc = PearsonsCorrelation()
        val result = mutableListOf<Triple<Int, Int, BigDecimal>>()
        for ((x, data1) in prices.values.withIndex()) {
            for ((y, data2) in prices.values.withIndex()) {
                if (y < x) continue
                val data = Pair(data1, data2).clean()
                if (data.first.size >= minObservations) {
                    val corr = calc.correlation(data.first, data.second)
                    val corrBD = BigDecimal(corr).setScale(scale, RoundingMode.HALF_DOWN)
                    result.add((Triple(x, y, corrBD)))
                    if (x != y) result.add((Triple(y, x, corrBD)))
                }
            }
        }
        return result
    }

    override fun renderOption(): String {
        val prices = collectPrices()
        val labels = prices.keys.map { it.symbol }
        val labelsData =  gsonBuilder.create().toJson(labels)

        val data = getMatrix(prices)
        val dataJson = gsonBuilder.create().toJson(data)

        return """
            {
              tooltip: {
                position: 'top',
                formatter: function (p) {
                      yLabel = option.yAxis.data[p.data[1]];    
                      return p.name + ':' + yLabel +" = " + p.data[2];
                }
              },
              ${renderGrid()},
               toolbox: {
                    feature: {
                        restore: {},
                        saveAsImage: {}
                    }
                },
              xAxis: {
                type: 'category',
                data: $labelsData
              },
              yAxis: {
                type: 'category',
                data: $labelsData
              },
              visualMap: {
                min: -1,
                max: 1,
                calculable: true,
                orient: 'horizontal',
                left: 'center',
                top: 'top',
                inRange : { color: ['#FF0000', '#00FF00'] }
              },
              series: [
                {
                  type: 'heatmap',
                  data: $dataJson,
                  label: {
                    show: true
                  },
                  emphasis: {
                    itemStyle: {
                      shadowBlur: 10,
                      shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                  }
                }
              ]
            }
        """.trimIndent()
    }
}