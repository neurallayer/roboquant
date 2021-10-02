package org.roboquant.jupyter

import org.roboquant.brokers.Trade
import java.math.BigDecimal
import java.time.Instant

/**
 * Trade chart plots the [trades] that have been generated during a run. By default, the realized pnl of the trades will
 * be plotted but this can be changed.
 *
 */
class TradeChart(
    private val trades: List<Trade>,
    private val skipBuy: Boolean = false,
    private val aspect: String = "pnl"
) : Chart() {

    private var max = Double.MIN_VALUE.toBigDecimal()

    init {
        require(
            aspect in listOf(
                "pnl",
                "fee",
                "amount",
                "quantity"
            )
        ) { "Unsupported aspect $aspect, valid options are pnl, fee, amount and quantity" }
    }

    private fun toSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (trade in trades) {
            if (skipBuy && trade.quantity > 0) continue
            with(trade) {
                val c = asset.currency
                val value = when (aspect) {
                    "pnl" -> c.toBigDecimal(pnl)
                    "fee" -> c.toBigDecimal(fee)
                    "amount" -> c.toBigDecimal(totalAmount)
                    "quantity" -> quantity.toBigDecimal()
                    else -> throw Exception("Unsupported aspect $aspect")
                }

                if (value.abs() > max) max = value.abs()
                val amount = c.format(totalAmount)
                val tooltip = "asset: $asset <br> qty: $quantity <br> pnl: $pnl <br> amount: $amount"
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