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

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.EventChannel
import org.roboquant.feeds.Feed
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Shows the correlation matrix between the prices of two or more [assets] that are present in the provided [feed]
 */
class PriceCorrelationChart(
    private val feed: Feed,
    private val assets: Collection<Asset>,
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT",
    private val scale: Int = 2,
    private val minObservations: Int = 3
) : Chart() {

    init {
        require(assets.size > 1) { "Minimum of 2 assets are required, found ${assets.size}" }
    }

    private fun Pair<List<Double>, List<Double>>.clean(): Pair<DoubleArray, DoubleArray> {
        val max = Integer.max(first.size, second.size)
        val r1 = mutableListOf<Double>()
        val r2 = mutableListOf<Double>()
        for (i in 0 until max) {
            if (first[i].isFinite() && second[i].isFinite()) {
                r1.add(first[i])
                r2.add(second[i])
            }
        }
        return Pair(r1.toDoubleArray(), r2.toDoubleArray())
    }


    private fun collectPrices(): Map<Asset, List<Double>> = runBlocking {
        val channel = EventChannel(timeframe = timeframe)
        val result = TreeMap<Asset, MutableList<Double>>()

        val job = launch {
            feed.play(channel)
            channel.close()
        }

        try {
            while (true) {
                val o = channel.receive()
                val prices = o.prices
                for (asset in assets) {
                    val price = prices[asset]?.getPrice(priceType) ?: Double.NaN
                    val list = result.getOrPut(asset) { mutableListOf() }
                    list.add(price)
                }
            }

        } catch (_: ClosedReceiveChannelException) {

        } finally {
            channel.close()
            if (job.isActive) job.cancel()
        }
        return@runBlocking result
    }

    private fun getMatrix(prices: Map<Asset, List<Double>>): List<Triple<Int, Int, BigDecimal>> {
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

    /** @suppress */
    override fun renderOption(): String {
        val prices = collectPrices()
        val labels = prices.keys.map { it.symbol }
        val labelsData = gsonBuilder.create().toJson(labels)

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
              title: {
                    text: '${assets.size} assets $timeframe'
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
                min: -1.0,
                max: 1.0,
                precision: 2,
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