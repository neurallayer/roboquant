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

import org.roboquant.brokers.Trade
import org.roboquant.common.UnsupportedException
import java.math.BigDecimal
import java.time.Instant

/**
 * Trade chart plots the trades of an [trades] that have been generated during a run. By default, the realized pnl of
 * the trades will be plotted but this can be changed. The possible options are pnl, fee, cost and quantity
 */
open class TradeChart(
    private val trades: List<Trade>,
    private val aspect: String = "pnl",
) : Chart() {

    private var max = Double.MIN_VALUE.toBigDecimal()

    init {
        val validAspects = listOf("pnl", "fee", "cost", "quantity")
        require(aspect in validAspects) { "Unsupported aspect $aspect, valid values are $validAspects" }
    }

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

    private fun toSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (trade in trades.sortedBy { it.time }) {
            with(trade) {
                val value = when (aspect) {
                    "pnl" -> pnl.convert(time = time).toBigDecimal()
                    "fee" -> fee.convert(time = time).toBigDecimal()
                    "cost" -> totalCost.convert(time = time).toBigDecimal()
                    "quantity" -> size.toBigDecimal()
                    else -> throw UnsupportedException("Unsupported aspect $aspect")
                }

                if (value.abs() > max) max = value.abs()
                val tooltip = getTooltip(this)
                d.add(Triple(time, value, tooltip))
            }
        }

        return d
    }

    override fun renderOption(): String {
        val gson = gsonBuilder.create()
        max = Double.MIN_VALUE.toBigDecimal()

        val d = toSeriesData()
        val data = gson.toJson(d)
        val series = """
            {
                type: 'scatter',
                symbolSize: 10,
                data : $data
            }
        """

        return """
            {
                xAxis: {
                    type: 'time',
                    scale: true
                },
                title: {
                    text: 'Trade Chart $aspect'
                },
                yAxis: {
                    type: 'value',
                    scale: true
                },
                visualMap: {
                   min: -$max,
                   max: $max,
                   calculable: true,
                   orient: 'horizontal',
                   left: 'center',
                   dimension: 1,
                   top: 'top',
                   inRange : { color: ['#FF0000', '#00FF00'] }
                },
                tooltip: {
                     formatter: function (params) {
                        return params.value[2];
                     }
                },
                ${renderDataZoom()},
                ${renderToolbox(false)},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}
