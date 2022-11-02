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
import io.polygon.kotlin.sdk.rest.reference.SupportedTickersParameters
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
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
     * API key to access polygon.io
     */
    var key: String = Config.getProperty("polygon.key", "")
)

/**
 * Historic data feed using market data from Polygon.io
 */
class PolygonHistoricFeed(
    configure: PolygonConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = PolygonConfig()
    private var client: PolygonRestClient

    /**
     * Get available assets
     */
    val availableAssets : List<Asset> by lazy {
        availableAssets()
    }

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = PolygonRestClient(config.key)
    }


    private fun availableAssets(): List<Asset> {
        val assets = mutableListOf<Asset>()
        var done = false
        var lastSymbol = ""

        while (! done) {
            val params = SupportedTickersParameters(limit = 10000, market = "stocks", tickerGT = lastSymbol)
            val tickers = client.referenceClient.getSupportedTickersBlocking(params)
            for (result in tickers.results!!) {
                val currency = result.currencyName?.uppercase() ?: "USD"
                val exchange = result.primaryExchange?.uppercase() ?: ""

                val assetType = when(result.market) {
                    "stocks" -> AssetType.STOCK
                    "crypto" -> AssetType.CRYPTO
                    "fx" -> AssetType.FOREX
                    else -> continue
                }

                val asset = Asset(
                    result.ticker.toString(),
                    assetType,
                    currency,
                    exchange
                )
                assets.add(asset)
            }
            done = tickers.results!!.isEmpty()
            lastSymbol = assets.last().symbol
            Thread.sleep(12_000)
        }

        return assets
    }


    /**
     * Retrieve [PriceBar] data for the provided [symbols] and [timeframe].
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe,
        multiplier: Int = 1,
        timespan: String = "day",
        limit: Int = 5000
    ) {
        for (symbol in symbols) {
            val aggr = client.getAggregatesBlocking(
                AggregatesParameters(
                    symbol,
                    multiplier.toLong(),
                    timespan,
                    timeframe.start.toEpochMilli().toString(),
                    timeframe.end.toEpochMilli().toString(),
                    limit = limit.toLong()
                )
            )

            val asset = Asset(symbol)
            for (bar in aggr.results) {
                val action =
                    PriceBar(asset, doubleArrayOf(bar.open!!, bar.high!!, bar.low!!, bar.close!!, bar.volume!!))
                val time = Instant.ofEpochMilli(bar.timestampMillis!!)
                add(time, action)
            }
        }
    }

}