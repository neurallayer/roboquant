/*
 * Copyright 2022 Neural Layer
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

import org.icepear.echarts.Option
import org.icepear.echarts.Scatter
import org.icepear.echarts.charts.scatter.ScatterSeries
import org.icepear.echarts.components.coord.cartesian.TimeAxis
import org.icepear.echarts.components.coord.cartesian.ValueAxis
import org.icepear.echarts.components.dataZoom.DataZoom
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.orders.OrderState
import org.roboquant.orders.OrderStatus
import org.roboquant.orders.SingleOrder
import java.math.BigDecimal
import java.time.Instant

/**
 * Order chart plots [orders] sizes over time. Most comon use case is to plot the order sizes of a single asset, but
 * this is not a strict requirement.
 *
 * Please not this chart only display orders of the type [SingleOrder] and will ignore other order types. Often trades
 * provide more insights, since these also cover more advanced order types. You can use the [TradeChart] for that.
 */
class OrderChart(
    private val orderStates: List<OrderState>,
) : Chart() {

    private fun getTooltip(order: SingleOrder, openedAt: Instant): String {

        return with(order) {
            """
                |asset: ${asset.symbol}<br>
                |currency: ${asset.currency}<br>
                |placed: $openedAt<br>
                |size: ${order.size}<br> 
                |id: $id<br> 
                |type: ${order::class.simpleName}<br> 
                |tif: ${order.tif}""".trimMargin()
        }
    }

    private fun toSeriesData(): List<Triple<Instant, BigDecimal, String>> {
        val states = orderStates.filter { it.status != OrderStatus.INITIAL }
        val d = mutableListOf<Triple<Instant, BigDecimal, String>>()
        for (state in states.sortedBy { it.openedAt }) {
            val order = state.order
            if (order is SingleOrder) {
                val value = order.size.toBigDecimal()
                val tooltip = getTooltip(order, state.openedAt)
                d.add(Triple(state.openedAt, value, tooltip))
            }
        }

        return d
    }

    /** @suppress */
    override fun getOption(): Option {

        val data = toSeriesData()
        val max = data.maxOfOrNull { it.second }
        val min = data.minOfOrNull { it.second }

        val series = ScatterSeries()
            .setData(data)
            .setSymbolSize(10)

        val vm = getVisualMap(min, max).setDimension(1)

        val tooltip = Tooltip()
            .setFormatter(javasciptFunction("return p.value[2];"))

        val chart = Scatter()
            .setTitle(title ?: "Order size")
            .addXAxis(TimeAxis())
            .addYAxis(ValueAxis().setScale(true))
            .addSeries(series)
            .setVisualMap(vm)
            .setTooltip(tooltip)

        val option = chart.option
        option.setToolbox(getToolbox(includeMagicType = false))
        option.setDataZoom(DataZoom())

        return option
    }
}