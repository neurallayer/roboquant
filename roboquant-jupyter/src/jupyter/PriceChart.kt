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

import org.roboquant.brokers.Trade
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import java.math.BigDecimal
import java.time.Instant

/**
 * Plot the prices of an [asset] found in the [feed] and optionally the [trades] made for that same asset.
 *
 */
class PriceChart(
    private val feed: Feed,
    private val asset: Asset,
    private val trades: Collection<Trade> = emptyList(),
    private val timeframe: Timeframe = Timeframe.INFINITE,
    private val priceType: String = "DEFAULT"
) : Chart() {

    /**
     * Play the feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     */
    private fun fromFeed(): List<Pair<Instant, BigDecimal>> {
        val entries = feed.filter<PriceAction>(timeframe) { it.asset == asset }
        val data = entries.map {
            val price = it.second.getPriceAmount(priceType)
            it.first to price.toBigDecimal()
        }
        return data
    }

    private fun markPoints(): List<Map<String, Any>> {
        val t = trades.filter { it.asset == asset && timeframe.contains(it.time) }
        val result = mutableListOf<Map<String, Any>>()
        for (trade in t) {
            val entry = mapOf(
                "value" to trade.size.toBigDecimal(),
                "xAxis" to trade.time,
                "yAxis" to Amount(trade.asset.currency, trade.price).toBigDecimal()
            )
            result.add(entry)
        }
        return result
    }

    /** @suppress */
    override fun renderOption(): String {
        val line = reduce(fromFeed())
        val lineData = gsonBuilder.create().toJson(line)
        val timeframe = if (line.size > 1) Timeframe(line.first().first, line.last().first).toString() else ""

        val marks = markPoints()
        val markData = gsonBuilder.create().toJson(marks)

        val series = """
            {
                name: '${asset.symbol}',
                type: 'line',
                showSymbol: false,
                lineStyle: {
                    width: 1
                },
                data : $lineData,
                markPoint: {
                    data: $markData
                },
            },
        """

        return """
            {
                xAxis: {
                    type: 'time',
                    scale: true
                },
                yAxis: {
                    type: 'value',
                    scale: true
                },
                 title: {
                    text: '${asset.symbol} $timeframe'
                },
                tooltip: {
                    trigger: 'axis'
                },
                ${renderDataZoom()},
                ${renderToolbox()},
                ${renderGrid()},  
                series : [$series]
            }
       """.trimStart()
    }

}