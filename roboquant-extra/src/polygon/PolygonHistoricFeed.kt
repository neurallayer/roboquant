/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.polygon

import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Timeframe
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Configuration for [PolygonHistoricFeed]
 */
data class PolygonConfig(

    /**
     * API key to access polygon.io api's
     */
    var key: String = Config.getProperty("polygon.key", "")
)

/**
* Historic data feed using market data from Polygon.io
*/
class PolygonHistoricFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    val config = PolygonConfig()
    private var client: PolygonRestClient

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = PolygonRestClient(config.key)
    }

    /**
     * Retrieve [PriceBar] data for the provided [symbol]
     */
    fun retrieve(symbol: String, tf: Timeframe, multiplier:Int = 1, timespan: String = "day", limit: Int = 5000) {
        val aggr = client.getAggregatesBlocking(
            AggregatesParameters(
                symbol,
                multiplier.toLong(),
                timespan,
                tf.start.toEpochMilli().toString(),
                tf.end.toEpochMilli().toString(),
                limit = limit.toLong()
            )
        )

        for (bar in aggr.results) {
            val action =
                PriceBar(Asset(symbol), doubleArrayOf(bar.open!!, bar.high!!, bar.low!!, bar.close!!, bar.volume!!))
            val time = Instant.ofEpochMilli(bar.timestampMillis!!)
            add(time, action)
        }
    }

}