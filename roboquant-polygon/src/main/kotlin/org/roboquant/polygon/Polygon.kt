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

import io.polygon.kotlin.sdk.rest.PolygonRestClient
import io.polygon.kotlin.sdk.rest.reference.SupportedTickersParameters
import io.polygon.kotlin.sdk.rest.reference.TickerDTO
import io.polygon.kotlin.sdk.websocket.*
import org.roboquant.common.*


/**
 * Configuration for Polygon connections
 *
 * @property key the polygon api key to use (property name is polygon.key)
 * @property delayed use a delayed feed, default is true
 */
data class PolygonConfig(
    var key: String = Config.getProperty("polygon.key", ""),
    var delayed: Boolean =  Config.getProperty("polygon.delayed", true)
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

        val feed = if (config.delayed) Feed.Delayed else Feed.RealTime
        val market = Market.Stocks

        val websocketClient = PolygonWebSocketClient(
            config.key,
            feed,
            market,
            object : PolygonWebSocketListener {

                override fun onAuthenticated(client: PolygonWebSocketClient) {
                    logger.info("Authenticated")
                }

                override fun onReceive(client: PolygonWebSocketClient, message: PolygonWebSocketMessage) {
                    logger.trace { message.toString() }
                    handler(message)
                }

                override fun onDisconnect(client: PolygonWebSocketClient) {
                    logger.info("Disconnected")
                }

                override fun onError(client: PolygonWebSocketClient, error: Throwable) {
                    logger.warn(error) { "websocket error" }
                }

            })

        return websocketClient

    }

    private fun TickerDTO.toAsset(): Asset? {

        val assetType = when (market) {
            "stocks" -> AssetType.STOCK
            "crypto" -> AssetType.CRYPTO
            "fx" -> AssetType.FOREX
            else -> return null
        }

        val currency = currencyName?.uppercase() ?: "USD"
        val exchange = primaryExchange?.uppercase() ?: ""

        return Asset(
            ticker.toString(),
            assetType,
            currency,
            exchange
        )
    }


    internal fun String.toAsset() : Asset {
        return when {
            startsWith("O:") -> Asset(drop(2), type = AssetType.OPTION)
            else -> Asset(this)
        }
    }

    internal fun availableAssets(client: PolygonRestClient): List<Asset> {
        val assets = mutableListOf<Asset>()
        val params = SupportedTickersParameters(
            limit = 1000,
            market = "stocks",
            sortBy = "ticker",
            sortDescending = false,
            activeSymbolsOnly = true,
        )

        client.referenceClient.listSupportedTickers(params).asSequence().forEach {
            val asset = it.toAsset()
            assets.addNotNull(asset)
        }

        return assets
    }

}
