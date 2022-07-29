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

import org.icepear.echarts.Option
import org.icepear.echarts.Pie
import org.icepear.echarts.Sunburst
import org.icepear.echarts.charts.pie.PieSeries
import org.icepear.echarts.charts.sunburst.SunburstSeries
import org.icepear.echarts.components.tooltip.Tooltip
import org.roboquant.brokers.Account
import java.math.BigDecimal

/**
 * Plot the allocation of assets as a pie chart
 *
 * @property account the account to use
 * @property includeCash also include cash balances next to the assets in the portfolio, default is true
 * @property includeAssetClass group per assetClass, default is false
 * @constructor Create a new asset allocation chart
 */
class AssetAllocationChart(
    private val account: Account,
    private val includeCash: Boolean = true,
    private val includeAssetClass: Boolean = false,
) : Chart() {


    private class Entry(val name: String, val value: BigDecimal, val type: String) {
        fun toMap() = mapOf("name" to name, "value" to value, "type" to type)
    }

    private fun toSeriesData(): List<Entry> {
        val result = mutableListOf<Entry>()
        if (includeCash) {
            for (amount in account.cash.toAmounts()) {
                val localAmount = account.convert(amount).toBigDecimal()
                result.add(Entry(amount.currency.displayName, localAmount, "CASH"))
            }
        }

        for (position in account.positions.sortedBy { it.asset.symbol }) {
            val asset = position.asset
            val localAmount = account.convert(position.exposure).toBigDecimal()
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
            .setTitle("Asset allocation")
            .addSeries(series)
            .setTooltip(Tooltip())

        val option = chart.option
        option.setToolbox(getBasicToolbox())
        return option
    }


    private fun renderSunburst(): Option  {

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
    override fun renderOption(): String {
        val result = if (includeAssetClass) renderSunburst() else renderPie()
        return renderJson(result)
    }
}