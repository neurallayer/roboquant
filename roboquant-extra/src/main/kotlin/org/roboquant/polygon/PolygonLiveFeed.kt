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

import io.polygon.kotlin.sdk.websocket.*
import kotlinx.coroutines.runBlocking
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.*
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
 * subscriptions at Polygon.io since it uses the websocket API.
 *
 * @TODO still needs to be tested.
 */
class PolygonLiveFeed(
    configure: PolygonConfig.() -> Unit = {}
) : LiveFeed() {

    private val config = PolygonConfig()
    private var client: PolygonWebSocketClient
    private val logger = Logging.getLogger(PolygonLiveFeed::class)

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }
        client = getWebSocketClient(config, this::handler)
        runBlocking {
            client.connect()
        }
    }

    /**
     * Handle incoming messages
     */
    private fun handler(message: PolygonWebSocketMessage) {

        when (message) {
            is PolygonWebSocketMessage.RawMessage -> logger.info(String(message.data))
            is PolygonWebSocketMessage.StocksMessage.Aggregate -> {
                val asset = Asset(message.ticker.toString())
                val action = PriceBar(
                    asset, message.openPrice!!, message.highPrice!!,
                    message.lowPrice!!, message.closePrice!!, message.volume!!
                )
                send(Event(listOf(action), Instant.now()))
            }

            is PolygonWebSocketMessage.StocksMessage.Trade -> {
                val asset = Asset(message.ticker.toString())
                val action = TradePrice(asset, message.price!!, message.size ?: Double.NaN)
                send(Event(listOf(action), Instant.now()))
            }

            is PolygonWebSocketMessage.StocksMessage.Quote -> {
                val asset = Asset(message.ticker.toString())
                val action = PriceQuote(
                    asset,
                    message.askPrice!!,
                    message.askSize ?: Double.NaN,
                    message.bidPrice!!,
                    message.bidSize ?: Double.NaN,
                )
                send(Event(listOf(action), Instant.now()))
            }

            else -> logger.warn { "received message=$message" }
        }
    }

    /**
     * Subscribe to the [symbols] for the specified action [type]
     */
    suspend fun subscribe(vararg symbols: String, type: PolygonActionType = PolygonActionType.TRADE) {
        val subscriptions = when (type) {

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

        client.subscribe(subscriptions)
    }

    /**
     * Disconnect from Polygon server and stop receiving market data
     */
    suspend fun disconnect() {
        client.disconnect()
    }


}