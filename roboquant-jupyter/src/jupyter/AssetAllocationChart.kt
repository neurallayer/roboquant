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
import java.math.BigDecimal

/**
 * Plot the allocation of assets as a pie chart

 * @property account the account to use
 * @property includeCash also include cash balances next to the portfolio, default is true
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

    private fun renderPie(): String {
        val gson = gsonBuilder.create()
        val d = toSeriesData().map { it.toMap() }
        val data = gson.toJson(d)

        val series = """
             {
                type: 'pie',
                radius: '80%',
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                },
                data : $data
            }
        """.trimIndent()

        return """
            {
                title: { text: 'Asset allocation' },
                tooltip : {},
                ${renderToolbox(false)},
                ${renderGrid()},  
                series : [$series]
            }
       """
    }

    /**
     * This custom toolbox enables to switch between a sunburst and treemap view of the asset allocation
     */
    @Suppress("MaxLineLength")
    private val toolbox = """
        toolbox: {
            feature: {
              dataZoom: { yAxisIndex: 'none' },
              dataView: {readOnly: true},
              saveAsImage: {},
              restore: {},
              myFeature: {
                show: true,
                title: 'Pie/Map',
                icon: 'path://M432.45,595.444c0,2.177-4.661,6.82-11.305,6.82c-6.475,0-11.306-4.567-11.306-6.82s4.852-6.812,11.306-6.812C427.841,588.632,432.452,593.191,432.45,595.444L432.45,595.444z M421.155,589.876c-3.009,0-5.448,2.495-5.448,5.572s2.439,5.572,5.448,5.572c3.01,0,5.449-2.495,5.449-5.572C426.604,592.371,424.165,589.876,421.155,589.876L421.155,589.876z M421.146,591.891c-1.916,0-3.47,1.589-3.47,3.549c0,1.959,1.554,3.548,3.47,3.548s3.469-1.589,3.469-3.548C424.614,593.479,423.062,591.891,421.146,591.891L421.146,591.891zM421.146,591.891',
                onclick: function () {
                    let currentType = myChart.getOption().series[0].type;
                    option.series[0].type = currentType === 'sunburst' ? 'treemap' : 'sunburst';
                    myChart.setOption(option);
                }
              }
            }
        }""".trimIndent()

    private fun renderSunburst(): String {
        val gson = gsonBuilder.create()
        val d = toSeriesData().groupBy { it.type }
            .map { entry -> mapOf("name" to entry.key, "children" to entry.value.map { it.toMap() }) }
        val data = gson.toJson(d)

        val series = """
             {
                type: 'sunburst',
                radius: '80%',
                data : $data
            }
            """.trimIndent()

        return """
            {
                title: { text: 'Asset allocation' },
                tooltip : {},
                $toolbox,
                series : [$series]
            }
            """.trimIndent()
    }

    /** @suppress */
    override fun renderOption(): String {
        return if (includeAssetClass) renderSunburst() else renderPie()
    }
}