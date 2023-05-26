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

import org.roboquant.common.*
import org.roboquant.feeds.Feed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.apply
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeries
import org.ta4j.core.BaseBarSeriesBuilder

class Ta4jIndicator(private val maxBarCount: Int = -1, private val block: (BarSeries) -> Map<String, Double>) {

    private fun MutableMap<Asset, BaseBarSeries>.getSeries(asset: Asset): BaseBarSeries {
        return getOrPut(asset) {
            val series = BaseBarSeriesBuilder().withName(asset.symbol).build()
            if (maxBarCount >= 0) series.maximumBarCount = maxBarCount
            series
        }
    }

    fun run(feed: Feed, timeframe: Timeframe = Timeframe.INFINITE): Map<String, TimeSeries> {
        val data = mutableMapOf<Asset, BaseBarSeries>()
        val result = mutableMapOf<String, MutableList<Observation>>()
        feed.apply<PriceBar>(timeframe = timeframe) { price, time ->
            val asset = price.asset
            val series = data.getSeries(asset)
            series.addBar(time.toUTC(), price.open, price.high, price.low, price.close, price.volume)
            val metric = block(series)
            for ((key, value) in metric) {
                val k = "$key.${asset.symbol.lowercase()}"
                val l = result.getOrPut(k) { mutableListOf() }
                l.add(Observation(time, value))
            }
        }
        return result.mapValues { TimeSeries(it.value) }
    }

}