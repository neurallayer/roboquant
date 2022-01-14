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

import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.filter

/**
 * Use the assets and prices found in the [feed] to plot the assets, their returns and trading volume. This chart for
 * now only works on feeds that contain [PriceBar] actions with known volumes.
 *
 */
class AssetPerformanceChart(
    private val feed: Feed,
    private val timeFrame: TimeFrame = TimeFrame.INFINITY,
    private val priceType: String = "DEFAULT"
) : Chart() {

    /**
     * Play the feed and filter the provided asset for price bar data. The output is suitable for candle stock charts
     */
    private fun fromFeed(): List<Map<String, Any>> {
        val result = mutableMapOf<Asset, MutableList<Double>>()
        val entries = feed.filter<PriceBar>(timeFrame)
        entries.forEach {
            val priceBar = it.second
            val price = priceBar.getPriceAmount(priceType).value
            val record = result.getOrPut(priceBar.asset) { mutableListOf(price, 0.0, 0.0) } // start, last, volume
            record[1] = price
            record[2] = record[2] + (price * priceBar.volume)
        }
        return result.map {
            val returns = 100.0 * (it.value[1] - it.value[0])/it.value[0]
            mapOf("name" to it.key.symbol,  "value" to listOf(it.value[2], returns))
        }
    }

    /** @suppress */
    override fun renderOption(): String {
        val list = fromFeed()
        val max = list.maxOf {
            val x = it["value"] as List<*>
            x[1] as Double
        }

        val data = gsonBuilder.create().toJson(list)

        val series = """
            {
                name: 'Assets',
                type: 'treemap',
                data : $data,
            },
        """

        return """
            {
                title: {
                    text: 'Asset Performance'
                },
                visualMap: {
                   min: -$max,
                   max: $max,
                   dimension: 1,
                   calculable: true,
                   orient: 'horizontal',
                   left: 'center',
                   top: 'top',
                   inRange : { color: ['#FF0000', '#00FF00'] }
                },
                tooltip: {
                   position: 'top',
                   formatter: function (p) {
                        volume = (p.value[0] / 1000000).toFixed();
                        return 'asset: ' + p.name + '<br>' + 'volume: ' + volume + '<br>' + 'returns: ' + p.value[1]; 
                    }
                },
                toolbox: {
                    feature: {
                        restore: {},
                        saveAsImage: {}
                    }
                },
                series : [$series]
            }
       """.trimStart()
    }


}