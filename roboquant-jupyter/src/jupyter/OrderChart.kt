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

import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.SingleOrder
import java.math.BigDecimal
import java.time.Instant

/**
 * Order chart plots [orders] over time. By default, the quantity will be plotted, but you can change the [aspect]
 * to plot other properties of an order ("remaining", "direction", "quantity", "fill").
 *
 * Please not this chart only display orders of the type [SingleOrder] and will ignore other order types. Often trades
 * provide more insights, since these also cover advanced order types. You can use the [TradeChart] for that.
 */
class OrderChart(
    private val orderStates: List<OrderState>,
    private val aspect: String = "quantity",
) : Chart() {

    private var max = Double.MIN_VALUE.toBigDecimal()

    init {
        require(aspect in listOf("remaining", "direction", "quantity", "value"))
    }

    private fun getTooltip(order: SingleOrder, openedAt: Instant): String {
        return with(order) {
            // "asset: $asset <br> currency: ${asset.currency} <br> placed: $openedAt <br> id: $id <br> type: ${order::class.simpleName}"
            "asset: $asset <br> currency: ${asset.currency} <br> placed: $openedAt <br> qty: ${order.quantity} <br> id: $id <br> type: ${order::class.simpleName} <br> tif: ${order.tif}"
        }
    }

    private fun toSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        val states = orderStates.filter { it.status != OrderStatus.INITIAL }
        max = Double.MIN_VALUE.toBigDecimal()
        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (state in states.sortedBy { it.openedAt }) {
            val order = state.order
            if (order is SingleOrder) {
                val value = when (aspect) {
                    // "remaining" -> remaining.toBigDecimal()
                    "direction" -> order.direction.toBigDecimal()
                    "quantity" -> order.quantity.toBigDecimal()
                    // "value" -> order.value().convert(time = order.placed).toBigDecimal()
                    else -> throw Exception("Unsupported aspect")
                }

                if (value.abs() > max) max = value.abs()
                val tooltip = getTooltip(order, state.openedAt)
                d.add(Triple(state.openedAt, value, tooltip))
            }
        }

        return d
    }

    /** @suppress */
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
                ${renderToolbox(false)},
                ${renderGrid()},
                series : [$series]
            }
       """.trimIndent()
    }
}