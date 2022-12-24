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

import io.polygon.kotlin.sdk.rest.PolygonRestClient
import io.polygon.kotlin.sdk.rest.reference.SupportedTickersParameters
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketClient
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketCluster
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketListener
import io.polygon.kotlin.sdk.websocket.PolygonWebSocketMessage
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.Logging


/**
 * Configuration for Polygon connections
 */
data class PolygonConfig(

    /**
     * API key to access polygon.io
     */
    var key: String = Config.getProperty("polygon.key", "")
)

/**
 * Shared logic between polygon feeds
 */
internal object Polygon {

    private val logger = Logging.getLogger(PolygonLiveFeed::class)


    internal fun getRestClient(config: PolygonConfig): PolygonRestClient {
        return PolygonRestClient(config.key)
    }

    internal fun getWebSocketClient(
        config: PolygonConfig,
        handler: (message: PolygonWebSocketMessage) -> Unit
    ): PolygonWebSocketClient {

        val websocketClient = PolygonWebSocketClient(
            config.key,
            PolygonWebSocketCluster.Stocks,
            object : PolygonWebSocketListener {

                override fun onAuthenticated(client: PolygonWebSocketClient) {
                    logger.trace("Connected")
                }

                override fun onReceive(client: PolygonWebSocketClient, message: PolygonWebSocketMessage) {
                    logger.trace { message }
                    handler(message)
                }

                override fun onDisconnect(client: PolygonWebSocketClient) {
                    logger.trace("Disconnected")
                }

                override fun onError(client: PolygonWebSocketClient, error: Throwable) {
                    logger.warn(error) {}
                }

            })

        return websocketClient

    }


    internal fun availableAssets(client: PolygonRestClient): List<Asset> {
        val assets = mutableListOf<Asset>()
        var done = false

        while (!done) {
            val lastSymbol = assets.lastOrNull()?.symbol ?: ""
            val params = SupportedTickersParameters(
                limit = 1000,
                market = "stocks",
                activeSymbolsOnly = true,
                tickerGT = lastSymbol
            )
            val results = client.referenceClient.getSupportedTickersBlocking(params).results ?: emptyList()
            for (result in results) {
                val currency = result.currencyName?.uppercase() ?: "USD"
                val exchange = result.primaryExchange?.uppercase() ?: ""

                val assetType = when (result.market) {
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
            done = results.isEmpty()
        }
        return assets
    }


}