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
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant


/**
 * Live data feed using market data from Polygon.io
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
        client = getClient(config.key)
        runBlocking {
            client.connect()
        }
    }

    private fun getClient(polygonKey: String): PolygonWebSocketClient  {
        val websocketClient = PolygonWebSocketClient(
            polygonKey,
            PolygonWebSocketCluster.Stocks,
            object : PolygonWebSocketListener {
                override fun onAuthenticated(client: PolygonWebSocketClient) {
                    logger.trace("Connected!")
                }

                override fun onReceive(
                    client: PolygonWebSocketClient,
                    message: PolygonWebSocketMessage
                ) {


                    when (message) {
                        is PolygonWebSocketMessage.RawMessage -> println(String(message.data))
                        is PolygonWebSocketMessage.StocksMessage.Aggregate -> {
                            val asset = Asset(message.ticker.toString())
                            val action = PriceBar(
                                asset, message.openPrice!!, message.highPrice!!,
                                message.lowPrice!!, message.closePrice!!, message.volume!!
                            )
                            send(Event(listOf(action), Instant.now()))
                        }
                        else -> println("Received Message: $message")
                    }
                }

                override fun onDisconnect(client: PolygonWebSocketClient) {
                    logger.trace("Disconnected!")
                }

                override fun onError(client: PolygonWebSocketClient, error: Throwable) {
                    logger.warn(error) {}
                }

            })

        return websocketClient

    }

    suspend fun subscribe(vararg symbols: String) {
        val subscriptions = symbols.map {
            PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.Trades, it)
        }

        client.subscribe(subscriptions)
    }

    suspend fun disconnect() {
        client.disconnect()
    }


}