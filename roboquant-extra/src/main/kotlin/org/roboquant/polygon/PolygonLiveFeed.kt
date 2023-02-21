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

package org.roboquant.polygon

import io.polygon.kotlin.sdk.websocket.*
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import org.roboquant.polygon.Polygon.availableAssets
import org.roboquant.polygon.Polygon.getRestClient
import org.roboquant.polygon.Polygon.getWebSocketClient
import java.time.Instant

/**
 * Types of Price actions that can be subscribed to
 */
enum class PolygonActionType {

    /**
     * [TradePrice] actions
     */
    TRADE,

    /**
     * [PriceQuote] actions
     */
    QUOTE,

    /**
     * [PriceBar] actions aggregated per minute
     */
    BAR_PER_MINUTE,

    /**
     * [PriceBar] actions aggregated per second
     */
    BAR_PER_SECOND
}

/**
 * Live data feed using market data from Polygon.io. This feed requires one of the non-free
 * subscriptions from Polygon.io since it uses the websocket API.
 *
 * @param configure additional configuration logic
 * @property useComputerTime use the computer time to stamp events or use the polygon supplied timestamps,
 * default is true
 */
class PolygonLiveFeed(
    configure: PolygonConfig.() -> Unit = {},
    private val useComputerTime: Boolean = true
) : LiveFeed() {

    private val config = PolygonConfig()
    private var client: PolygonWebSocketClient
    private val logger = Logging.getLogger(PolygonLiveFeed::class)
    private val subscriptions = mutableMapOf<String, Asset>()

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = getWebSocketClient(config, this::handler)
        runBlocking {
            client.connect()
        }
    }

    /**
     * Return the available assets. Due to the amount of API calls made, this requires a
     * non-free subscription at Polygon.io
     */
    val availableAssets: List<Asset> by lazy {
        availableAssets(getRestClient(config))
    }


    private fun getTime(endTime: Long?): Instant {
        return if (useComputerTime || endTime == null) Instant.now() else Instant.ofEpochMilli(endTime)
    }

    /**
     * Get the full asset based on the symbol (aka ticker)
     */
    private fun getSubscribedAsset(symbol: String?): Asset {
        return subscriptions.getValue(symbol!!)
    }


    /**
     * Handle incoming messages
     */
    private fun handler(message: PolygonWebSocketMessage) {

        when (message) {
            is PolygonWebSocketMessage.RawMessage -> logger.info(String(message.data))
            is PolygonWebSocketMessage.StocksMessage.Aggregate -> {
                val asset = getSubscribedAsset(message.ticker)
                val action = PriceBar(
                    asset, message.openPrice!!, message.highPrice!!,
                    message.lowPrice!!, message.closePrice!!, message.volume ?: Double.NaN
                )
                send(Event(listOf(action), getTime(message.endTimestampMillis)))
            }

            is PolygonWebSocketMessage.StocksMessage.Trade -> {
                val asset = getSubscribedAsset(message.ticker)
                val action = TradePrice(asset, message.price!!, message.size ?: Double.NaN)
                send(Event(listOf(action), getTime(message.timestampMillis)))
            }

            is PolygonWebSocketMessage.StocksMessage.Quote -> {
                val asset = getSubscribedAsset(message.ticker)
                val action = PriceQuote(
                    asset,
                    message.askPrice!!,
                    message.askSize ?: Double.NaN,
                    message.bidPrice!!,
                    message.bidSize ?: Double.NaN,
                )
                send(Event(listOf(action), getTime(message.timestampMillis)))
            }

            else -> logger.warn { "received message=$message" }
        }
    }

    /**
     * Subscribe to the [symbols] for the specified action [type], default action is `PolygonActionType.TRADE`
     */
    suspend fun subscribe(vararg symbols: String, type: PolygonActionType = PolygonActionType.TRADE) {

        // val assets = symbols.map { symbol -> availableAssets.first { it.symbol ==  symbol } }
        // associateBy { it.symbol }
        val assets = symbols.map { Asset(it) }.associateBy { it.symbol }
        subscriptions.putAll(assets)

        val polygonSubs = when (type) {

            PolygonActionType.TRADE -> symbols.map {
                PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.Trades, it)
            }

            PolygonActionType.QUOTE -> symbols.map {
                PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.Quotes, it)
            }

            PolygonActionType.BAR_PER_MINUTE -> symbols.map {
                PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.AggPerMinute, it)
            }

            PolygonActionType.BAR_PER_SECOND -> symbols.map {
                PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.AggPerSecond, it)
            }

        }

        client.subscribe(polygonSubs)
    }

    /**
     * Disconnect from Polygon server and stop receiving market data
     */
    suspend fun disconnect() {
        client.disconnect()
    }


}