/*
 * Copyright 2020-2023 Neural Layer
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
@file:Suppress("LongParameterList")

package org.roboquant.polygon

import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.polygon.Polygon.availableAssets
import org.roboquant.polygon.Polygon.getRestClient
import org.roboquant.polygon.Polygon.toAsset
import java.time.Instant


/**
 * Historic data feed using market data from Polygon.io. If using a free subscription at Polygon.io, be aware
 * of the API call rate limits imposed.
 */
class PolygonHistoricFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = PolygonConfig()
    private var client: PolygonRestClient

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = getRestClient(config)
    }

    /**
     * Return the available assets. Due to the number of API calls made, this requires a
     * non-free subscription at Polygon.io
     *
     * Also, this will only include stocks, and not derivatives like options.
     */
    val availableAssets: List<Asset> by lazy {
        availableAssets(client)
    }

    /**
     * Retrieve [PriceBar] data for the provided [symbols] and [timeframe].
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe,
        multiplier: Int = 1,
        timespan: String = "day",
        limit: Int = 5000,
        adjusted: Boolean = true
    ) {
        for (symbol in symbols) {
            val aggr = client.getAggregatesBlocking(
                AggregatesParameters(
                    symbol,
                    multiplier.toLong(),
                    timespan,
                    timeframe.start.toEpochMilli().toString(),
                    timeframe.end.toEpochMilli().toString(),
                    !adjusted,
                    limit.toLong()
                )
            )

            val tp = when (timespan) {
                "day" -> multiplier.days
                "minute" -> multiplier.minutes
                "hour" -> multiplier.hours
                else -> null
            }

            val asset = symbol.toAsset()

            for (bar in aggr.results) {
                val action =
                    PriceBar(asset, doubleArrayOf(bar.open!!, bar.high!!, bar.low!!, bar.close!!, bar.volume!!), tp)
                val time = Instant.ofEpochMilli(bar.timestampMillis!!)
                add(time, action)
            }
        }
    }

}