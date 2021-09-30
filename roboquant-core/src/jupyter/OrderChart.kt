package org.roboquant.jupyter

import org.roboquant.orders.Order
import org.roboquant.orders.SingleOrder
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import kotlin.math.absoluteValue

/**
 * Trade chart that plots [orders] over time. By default, the quantity will be plotted, but you can change the [aspect]
 * to plot other properties of an order ("remaining", "direction", "quantity", "fill").
 *
 * Please not this chart only display orders of the type SingleOrder and will ignore other order types. Often trades are
 * of more value, since these also cover advanced orders. You can use the [TradeChart] for that.
 */
class OrderChart(
    val orders: List<Order>,
    private val skipBuy: Boolean = false,
    val aspect: String = "quantity",
    private val scale: Int = 2
) : Chart() {

    private var max = Double.MIN_VALUE

    init {
        require(aspect in listOf("remaining", "direction", "quantity", "fill"))
    }

    private fun toSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        val singleOrders = orders.filterIsInstance<SingleOrder>()

        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (order in singleOrders) {
            if (skipBuy && order.quantity > 0) continue
            with(order) {

                val value = when (aspect) {
                    "remaining" -> remaining
                    "direction" -> order.direction.toDouble()
                    "quantity" -> quantity
                    "fill" -> fill
                    else -> throw Exception("Unsupported aspect")
                }

                if (value.absoluteValue > max) max = value.absoluteValue
                val roundedValue = BigDecimal(value).setScale(scale, RoundingMode.HALF_DOWN)
                val tooltip = "asset: $asset <br> qty: $quantity"
                d.add(Triple(placed, roundedValue, tooltip))
            }
        }

        return d
    }

    /** @suppress */
    override fun renderOption(): String {
        val gson = gsonBuilder.create()
        max = Double.MIN_VALUE

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
                    text: 'Order Chart $aspect'
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
                ${renderToolbox(false)},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}