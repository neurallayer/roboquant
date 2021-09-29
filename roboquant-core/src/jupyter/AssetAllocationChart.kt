package org.roboquant.jupyter

import org.roboquant.brokers.Account
import java.math.BigDecimal
import kotlin.math.absoluteValue

/**
 * Plot the allocation of assets as a pie chart

 * @property account
 * @constructor Create empty Asset allocation chart
 */
class AssetAllocationChart(
    val account: Account,
    private val includeCash: Boolean = true,
    private val includeAssetClass: Boolean = false
) : Chart() {


    private class Entry(val name: String, val value: BigDecimal, val type: String) {
        fun toMap() = mapOf("name" to name, "value" to value, "type" to type)
    }

    private fun toSeriesData(): List<Entry> {
        val result = mutableListOf<Entry>()
        if (includeCash) {
            for (entry in account.cash.toMap()) {
                val localAmount = account.convertToCurrency(entry.key, entry.value.absoluteValue)
                val roundedValue = account.baseCurrency.toBigDecimal(localAmount)
                result.add(Entry(entry.key.displayName, roundedValue, "CASH"))
            }
        }
        val positions = account.portfolio.positions.values
        for (position in positions) {
            val asset = position.asset
            val localAmount = account.convertToCurrency(asset.currency, position.exposure)
            val roundedValue = account.baseCurrency.toBigDecimal(localAmount)
            result.add(Entry(asset.symbol, roundedValue, asset.type.name))
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
                title: {
                    text: 'Asset allocation'
                },
                ${renderToolbox(false)},
                ${renderGrid()},  
                series : [$series]
            }
       """
    }


    private fun renderSunburst(): String {
        val gson = gsonBuilder.create()
        val d = toSeriesData().groupBy { it.type }
            .map { entry -> mapOf("name" to entry.key, "children" to entry.value.map { it.toMap() }) }
        val data = gson.toJson(d)

        val series = """
             {
                type: 'sunburst',
                radius: '80%',
                emphasis: {
                    focus: 'ancestor'
                },
                data : $data
            }
        """.trimIndent()

        return """
            {
                title: {
                    text: 'Asset allocation'
                },
                ${renderToolbox(false)},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }

    override fun renderOption(): String {
        return if (includeAssetClass) renderSunburst() else renderPie()
    }
}