/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.connector.client.WebSocketApiClient
import com.binance.connector.client.impl.WebSocketApiClientImpl
import com.binance.connector.client.utils.websocketcallback.WebSocketMessageCallback
import com.google.gson.JsonParser
import org.json.JSONObject
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.years
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Create a new feed based on price actions coming from the Binance exchange.
 *
 * @constructor
 *
 */
class BinanceHistoricFeed(configure: BinanceConfig.() -> Unit = {}) : HistoricPriceFeed() {

    private val logger = Logging.getLogger(BinanceHistoricFeed::class)
    private val config = BinanceConfig()
    private val factory: BinanceApiClientFactory
    private val client: BinanceApiRestClient
    private val assetMap: Map<String, Asset>

    init {
        config.configure()
        factory = Binance.getFactory(config)
        client = factory.newRestClient()
        assetMap = Binance.retrieveAssets(client)
    }

    /**
     * Get the available assets for retrieving market data
     */
    val availableAssets
        get() = assetMap.values

    /**
     * Retrieve [PriceBar] data for the provides [symbols]. It will retrieve the data for the provided [timeframe],
     * given that it doesn't exceed the limits enforced by Binance.
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe,
        interval: Interval = Interval.ONE_MINUTE,
        limit: Int = 1000
    ) {
        require(symbols.isNotEmpty()) { "You need to provide at least 1 symbol" }

        val timeSpan = Binance.interval2TimeSpan(interval)

        val startTime = timeframe.start.toEpochMilli()
        val endTime = timeframe.end.toEpochMilli() - 1
        for (symbol in symbols) {
            val asset = assetMap[symbol]
            require(asset != null) { "invalid symbol $symbol" }
            val bars = client.getCandlestickBars(symbol, interval, limit, startTime, endTime)
            for (bar in bars) {
                val action = PriceBar(
                    asset,
                    bar.open.toDouble(),
                    bar.high.toDouble(),
                    bar.low.toDouble(),
                    bar.close.toDouble(),
                    bar.volume.toDouble(),
                    timeSpan
                )
                val now = Instant.ofEpochMilli(bar.closeTime)
                add(now, action)
            }
            logger.debug { "Retrieved $asset for $timeframe" }

        }
    }

    fun retrieve2(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(1.years),
        interval: String = "1d",
        limit: Int = 1_000
    ) {

        val client: WebSocketApiClient = WebSocketApiClientImpl()
        client.connect((WebSocketMessageCallback { event: String ->
            val json = JsonParser.parseString(event).asJsonObject
            if (json["status"].asInt != 200) {
                println(json)
            } else {
                val symbolId = json["id"].asString
                val asset = assetMap[symbolId]!!
                val result = json["result"].asJsonArray
                for (row in result) {
                    val arr = row.asJsonArray
                    val time = Instant.ofEpochMilli(arr[6].asLong) // use the close time
                    val pb = PriceBar(
                        asset,
                        arr[1].asDouble,
                        arr[2].asDouble,
                        arr[3].asDouble,
                        arr[4].asDouble,
                        arr[5].asDouble
                    )
                    add(time, pb)

                }
            }
        }))


        for (symbol in symbols) {
            val asset = assetMap[symbol]
            require(asset != null) { "invalid symbol $symbol" }

            val params = JSONObject()
            params.put("startTime", timeframe.start.toEpochMilli())
            params.put("endTime", timeframe.end.toEpochMilli())
            params.put("limit", limit)
            params.put("requestId", symbol)

            client.market().klines(symbol, interval, params)
        }
    }


}

