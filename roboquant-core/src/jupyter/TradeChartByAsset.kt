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

import org.roboquant.brokers.Account
import org.roboquant.brokers.Trade
import org.roboquant.common.Asset

/**
 * Trade chart plots the trades in an [account] that have been generated during a run per Asset. By default, the realized pnl of the
 * trades will be plotted but this is configurable
 *
 */
class TradeChartByAsset(
    private val account: Account,
    private val aspect: String = "pnl",
    private val filter: (Trade) -> Boolean = { true }
) : Chart() {

    init {
        val validAspects = listOf("pnl", "fee", "cost", "quantity")
        require(aspect in validAspects) { "Unsupported aspect $aspect, valid values are $validAspects" }
    }

    private fun getTooltip(trade: Trade): String {
        val pnl = trade.pnl.toBigDecimal()
        val totalCost = trade.totalCost.toBigDecimal()
        val fee = trade.fee.toBigDecimal()
        return "asset: ${trade.asset} <br> currency: ${trade.asset.currency} <br> time: ${trade.time} <br> qty: ${trade.quantity} <br> fee: $fee <br> pnl: $pnl <br> cost: $totalCost <br> order: ${trade.orderId}"
    }

    private fun toSeriesData(trades: List<Trade>, assets: List<Asset>): List<List<Any>> {
        val d = mutableListOf<List<Any>>()
        for (trade in trades) {
            with(trade) {
                val value = when (aspect) {
                    "pnl" -> account.convert(pnl, time = time).toBigDecimal()
                    "fee" -> account.convert(fee, time = time).toBigDecimal()
                    "cost" -> account.convert(totalCost, time = time).toBigDecimal()
                    "quantity" -> quantity.toBigDecimal()
                    else -> throw Exception("Unsupported aspect $aspect")
                }

                val y = assets.indexOf(asset)
                val tooltip = getTooltip(this)
                d.add(listOf(time, y, value, tooltip))
            }
        }

        return d
    }

    override fun renderOption(): String {
        val gson = gsonBuilder.create()
        val trades = account.trades.filter(filter)
        val assets = trades.map { it.asset }.distinct().sortedBy { it.symbol }
        val d = toSeriesData(trades, assets)
        val data = gson.toJson(d)
        val series = """
            {
                type: 'scatter',
                symbolSize: 10,
                data : $data
            }
        """

        val yAxisData = gson.toJson(assets.map { it.symbol })

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
                    type: 'category',
                    data: $yAxisData,
                    axisLine: {
                      show: false
                    }
                },
                tooltip: {
                     formatter: function (params) {
                        return params.value[3];
                     }
                },
                toolbox: {
                    feature: {
                        dataZoom: { },
                        dataView: {readOnly: true},
                        restore: {},
                        saveAsImage: {}
                    }
                },
                ${renderDataZoom()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}