/*
 * Copyright 2020-2024 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.charts

import org.icepear.echarts.Option
import org.icepear.echarts.Pie
import org.icepear.echarts.Sunburst
import org.icepear.echarts.charts.pie.PieSeries
import org.icepear.echarts.charts.sunburst.SunburstSeries
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.brokers.Account
import org.roboquant.common.Currency

import java.math.BigDecimal

/**
 * Plot the allocation of assets in the provided account.
 *
 * @property account the account to use
 * @property includeAssetClass group per assetClass, default is false
 * in the positions.
 * @constructor Create a new asset allocation chart
 */
class AllocationChart(
    private val account: Account,
    private val includeAssetClass: Boolean = false,
    private val currency: Currency = account.baseCurrency
) : Chart() {

    private class Entry(val name: String, val value: BigDecimal, val type: String) {
        fun toMap() = mapOf("name" to name, "value" to value, "type" to type)
    }

    private fun toSeriesData(): List<Entry> {
        val positions = account.positions.values
        if (positions.isEmpty()) return emptyList()
        val result = mutableListOf<Entry>()

        for (position in positions.sortedBy { it.asset.symbol }) {
            val asset = position.asset
            val localAmount = position.exposure.convert(currency).toBigDecimal()
            result.add(Entry(asset.symbol, localAmount, asset.type.name))
        }
        return result
    }

    private fun renderPie(): Option {

        val data = toSeriesData().map { it.toMap() }
        val series = PieSeries()
            .setRadius("80%")
            .setData(data)

        val chart = Pie()
            .setTitle(title ?: "Asset allocation")
            .addSeries(series)
            .setTooltip(Tooltip())

        val option = chart.option
        option.setToolbox(getBasicToolbox())
        return option
    }

    private fun renderSunburst(): Option {

        val data = toSeriesData().groupBy { it.type }
            .map { entry -> mapOf("name" to entry.key, "children" to entry.value.map { it.toMap() }) }

        val series = SunburstSeries()
            .setData(data)
            .setRadius("80%")

        val chart = Sunburst()
            .setTitle("Asset Allocation")
            .addSeries(series)
            .setTooltip(Tooltip())

        val option = chart.option
        option.setToolbox(getBasicToolbox())

        return option
    }

    /** @suppress */
    override fun getOption(): Option {
        return if (includeAssetClass) renderSunburst() else renderPie()
    }
}
