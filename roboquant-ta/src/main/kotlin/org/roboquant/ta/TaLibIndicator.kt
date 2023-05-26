/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.ta

import org.roboquant.common.Asset
import org.roboquant.common.Observation
import org.roboquant.common.TimeSeries
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.apply

class TaLibIndicator(
    private val maxBarCount: Int = 20,
    private val block: TaLib.(series: PriceBarSerie) -> Map<String, Double>
) {

    fun run(feed: Feed, timeframe: Timeframe = Timeframe.INFINITE): Map<String, TimeSeries> {
        val buffers = mutableMapOf<Asset, PriceBarSerie>()
        val taLib = TaLib()
        val result = mutableMapOf<String, MutableList<Observation>>()
        feed.apply<PriceBar>(timeframe = timeframe) { price, time ->
            val asset = price.asset
            val buffer = buffers.getOrPut(asset) { PriceBarSerie(maxBarCount) }
            if (buffer.add(price)) {
                val metric = block.invoke(taLib, buffer)
                for ((key, value) in metric) {
                    val k = "$key.${asset.symbol.lowercase()}"
                    val l = result.getOrPut(k) { mutableListOf() }
                    l.add(Observation(time, value))
                }
            }
        }
        return result.mapValues { TimeSeries(it.value) }
    }

}