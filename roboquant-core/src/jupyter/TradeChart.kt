package org.roboquant.jupyter

import org.roboquant.brokers.Trade
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue

/**
 * Trade chart plots the trades that have been generated during the run over time.
 *
 * @property trades
 * @property skipBuy
 * @property aspect
 * @property scale
 * @constructor Create empty Trade chart
 */
class TradeChart(val trades: List<Trade>, private val skipBuy: Boolean = false, val aspect:String = "pnl", private val scale:Int = 2) : EChart() {

    private var max = Double.MIN_VALUE

    init {
        require(aspect in listOf("pnl", "fee", "amount", "quantity"))
    }

    private fun toSeriesData(): List<Triple<String, BigDecimal, String>> {
        val d = mutableListOf<Triple<String, BigDecimal, String>>()
        for (trade in trades) {
            if (skipBuy && trade.quantity > 0) continue
            with(trade) {

                val value = when (aspect) {
                    "pnl" -> pnl
                    "fee" -> fee
                    "amount" -> totalAmount
                    "quantity" -> quantity
                    else -> throw Exception("Unsupported aspect")
                }

                if (value.absoluteValue > max) max = value.absoluteValue
                val roundedValue = BigDecimal(value).setScale(scale, RoundingMode.HALF_DOWN)
                val amount = asset.currency.format(totalAmount)
                val tooltip = "asset: $asset <br> qty: $quantity <br> pnl: $pnl <br> amount: $amount"
                d.add(Triple(time.toString(), roundedValue, tooltip))
            }
        }

        return d
    }

    override fun renderOption(): String {
        val gson = gsonBuilder.create()

        val d = toSeriesData()
        val data = gson.toJson(d)
        val series = """
            {
                type: 'scatter',
                symbolSize: 10,
                data : $data
            }
        """

        val visualMap = """
            visualMap: {
                   min: -$max,
                   max: $max,
                   calculable: true,
                   orient: 'horizontal',
                   left: 'center',
                   dimension: 1,
                   top: 'top',
                   inRange : { color: ['#FF0000', '#00FF00'] }
               }
        """.trimIndent()

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
                $visualMap,
                tooltip: {
                     formatter: function (params) {
                        return params.value[2];
                     }
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}