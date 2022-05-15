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

package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
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
        factory = BinanceConnection.getFactory(config)
        client = factory.newRestClient()
        assetMap = BinanceConnection.retrieveAssets(client)
    }


    val availableAssets
        get() = assetMap.values


    /**
     * Retrieve [PriceBar] data for the provides [symbols]. It will retrieve the data for the provided [timeframe],
     * given that it doesn't exceed the limits enforced by Binance.
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe,
        interval: Interval = Interval.DAILY,
        limit: Int = 1000
    ) {
        require(symbols.isNotEmpty()) { "You need to provide at least 1 symbol" }
        val startTime = timeframe.start.toEpochMilli()
        val endTime = timeframe.end.toEpochMilli() - 1
        for (symbol in symbols) {
            val finalSymbol = symbol.replace("/", "")
            val asset = assetMap[finalSymbol]
            if (asset != null ) {
                val bars = client.getCandlestickBars(finalSymbol, interval, limit, startTime, endTime)
                for (bar in bars) {
                    val action = PriceBar(
                        asset,
                        bar.open.toDouble(),
                        bar.high.toDouble(),
                        bar.low.toDouble(),
                        bar.close.toDouble(),
                        bar.volume.toDouble()
                    )
                    val now = Instant.ofEpochMilli(bar.closeTime)
                    add(now, action)
                }
                logger.fine { "Retrieved $asset for $timeframe" }
            } else {
                logger.warning{ "$symbol not found" }
            }

        }
    }

    
}

