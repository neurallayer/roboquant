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

package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.CandlestickInterval
import org.roboquant.common.Asset
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Logging
import org.roboquant.feeds.AssetFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import java.io.Closeable
import java.time.Instant

typealias Interval = CandlestickInterval

/**
 * Create a new feed based on live price actions coming from the Binance exchange.
 *
 * @property useMachineTime
 * @constructor
 *
 */
class BinanceLiveFeed(
    private val useMachineTime: Boolean = true,
    configure: BinanceConfig.() -> Unit = {}
) :
    LiveFeed(), AssetFeed {

    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger(BinanceLiveFeed::class)
    private val closeables = mutableListOf<Closeable>()
    private val config = BinanceConfig()
    private val factory: BinanceApiClientFactory
    private val client: BinanceApiWebSocketClient
    private val assetMap: Map<String, Asset>
    val availableAssets
        get() = assetMap.values

    /**
     * Get the assets that has been subscribed to
     */
    override val assets
        get() = subscriptions.values.toSortedSet()

    init {
        config.configure()
        factory = BinanceConnection.getFactory(config)
        client = factory.newWebSocketClient()
        assetMap = BinanceConnection.retrieveAssets(factory.newRestClient())
        logger.debug { "Started BinanceFeed using web-socket client" }
    }

    /**
     * Subscribe to the [PriceBar] actions for one or more symbols
     *
     * @param symbols the currency pairs you want to subscribe to
     * @param interval the interval of the PriceBar. Default is  1 minute
     */
    fun subscribePriceBar(
        vararg symbols: String,
        interval: Interval = Interval.ONE_MINUTE
    ) {
        require(symbols.isNotEmpty()) { "You need to provide at least 1 currency pair" }
        for (symbol in symbols) {
            val finalSymbol = symbol.replace("/", "")
            val asset = assetMap[symbol]
            if (asset != null) {
                logger.info { "Subscribing to $symbol" }

                // API requires lowercase symbol
                val closable = client.onCandlestickEvent(finalSymbol.lowercase(), interval) {
                    handle(it)
                }
                closeables.add(closable)
                subscriptions[asset.symbol] = asset
            } else {
                throw ConfigurationException("Not found $symbol")
            }
        }
    }

    private fun handle(resp: CandlestickEvent) {
        if (!resp.barFinal) return

        logger.trace { "Received candlestick event for symbol ${resp.symbol}" }
        val asset = subscriptions[resp.symbol.uppercase()]
        if (asset != null) {
            val action = PriceBar(
                asset,
                resp.open.toDouble(),
                resp.high.toDouble(),
                resp.low.toDouble(),
                resp.close.toDouble(),
                resp.volume.toDouble()
            )
            val now = if (useMachineTime) Instant.now() else Instant.ofEpochMilli(resp.closeTime)
            val event = Event(listOf(action), now)
            send(event)
        } else {
            logger.warn { "Received CandlestickEvent for unexpected symbol ${resp.symbol}" }
        }
    }

    override fun close() {
        for (c in closeables) c.close()
        closeables.clear()
    }

}

