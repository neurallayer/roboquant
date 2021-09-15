package org.roboquant.jupyter

import org.roboquant.orders.Order
import org.roboquant.orders.SingleOrder
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue

/**
 * Trade chart that plots the orders over time. Right now this chart only displays orders of the type SingleOrder and
 * will ignore other order types.
 *
 * @property orders
 * @property skipBuy
 * @property aspect
 * @property scale
 * @constructor Create empty Trade chart
 */
class OrderChart(
    val orders: List<Order>,
    private val skipBuy: Boolean = false,
    val aspect: String = "quantity",
    private val scale: Int = 2
) : EChart() {

    private var max = Double.MIN_VALUE

    init {
        require(aspect in listOf("remaining", "direction", "quantity", "fill"))
    }

    private fun toSeriesData(): List<Triple<String, BigDecimal, String>> {
        val singleOrders = orders.filterIsInstance<SingleOrder>()

        val d = mutableListOf<Triple<String, BigDecimal, String>>()
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
                d.add(Triple(placed.toString(), roundedValue, tooltip))
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
                ${renderToolbox()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }
}