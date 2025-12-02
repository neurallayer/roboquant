/*
 * Copyright 2020-2025 Neural Layer
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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.websocket.marketdata.streams.news.NewsMarketDataListenerAdapter
import net.jacobpeterson.alpaca.websocket.marketdata.streams.news.NewsMarketDataWebsocketInterface
import org.roboquant.common.Asset
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Event
import org.roboquant.common.Logging
import org.roboquant.common.NewsItems
import org.roboquant.common.Stock
import org.roboquant.common.symbols
import org.roboquant.feeds.LiveFeed
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * AlpacaMarketNewsLiveFeed allows subscribing to live market news for a given list of assets/symbols.
 *
 * This uses the same subscription mechanism as [AlpacaLiveFeed] (taking vararg symbols and converting to a Set),
 * and connects to the Alpaca News WebSocket to receive live news articles.
 * @param configure optional configuration of the Alpaca connection via [AlpacaConfig]
 * @param autoConnect if true (default) it will automatically connect to the Alpaca News WebSocket upon creation
 * @constructor Creates a new instance of the AlpacaMarketNewsLiveFeed
 */
class AlpacaMarketNewsLiveFeed(
    configure: AlpacaConfig.() -> Unit = {},
    autoConnect: Boolean = true
) : LiveFeed() {

    private val config = AlpacaConfig()
    private var alpacaAPI: AlpacaAPI? = null
    private val logger = Logging.getLogger(AlpacaMarketNewsLiveFeed::class)

    /**
     * Holds the current set of subscribed symbols.
     */
    private val subscriptions: MutableSet<Asset> = mutableSetOf()

    init {
        config.configure()
        if (autoConnect) connect()
    }

    /**
     * Subscribe to market news for the provided [assets].
     * This mirrors the subscription mechanism used in [AlpacaLiveFeed].
     */
    fun subscribe(vararg assets: Asset ){
        subscriptions.clear()
        subscriptions.addAll(assets )
        // Forward subscriptions to the underlying websocket
        alpacaAPI?.newsMarketDataStream()?.newsSubscriptions = subscriptions.symbols.toSet()
        logger.info { "Subscribed to news for symbols=$subscriptions" }
    }

    /**
     * Utility to send a NewsItems event to listeners at the specified [time].
     */
    fun sendNews(time: Instant, items: NewsItems) {
        val event = Event(time, listOf(items))
        send(event)
    }

    private fun connect() {
        val api = alpacaAPI ?: Alpaca.getAPI(config).also { alpacaAPI = it }
        val connection = api.newsMarketDataStream()
        connectNews(connection)
    }

    private fun connectNews(connection: NewsMarketDataWebsocketInterface) {
        require(!connection.isConnected) { "already connected, disconnect first" }
        val timeoutMillis: Long = 5_000
        connection.setAutomaticallyReconnect(true)
        connection.connect()
        connection.waitForAuthorization(timeoutMillis, TimeUnit.MILLISECONDS)
        if (!connection.isValid) {
            throw ConfigurationException("couldn't establish $connection")
        } else {
            val listener = createNewsHandler()
            connection.setListener(listener)
            // Apply current subscriptions if any
            if (subscriptions.isNotEmpty()) connection.newsSubscriptions = subscriptions.symbols.toSet()
        }
    }

    private fun createNewsHandler(): NewsMarketDataListenerAdapter {
        return object : NewsMarketDataListenerAdapter() {
            override fun onNews(news: net.jacobpeterson.alpaca.model.websocket.marketdata.streams.news.model.news.NewsMessage) {
                val item = NewsItems.NewsItem(
                    id = news.id.toString(),
                    content = news.content,
                    headline = news.headline,
                    assets = news.symbols.map { Stock(it) },
                    url = news.url,
                    meta = mapOf(
                        "source" to news.source,
                        "createdAt" to news.createdAt,
                        "updatedAt" to news.updatedAt,
                    )
                )
                sendNews(Instant.now(), NewsItems(listOf(item)))
            }
        }
    }

    /**
     * Close the connection to Alpaca News WebSocket
     */
    override fun close() {
        try {
            val c = alpacaAPI?.newsMarketDataStream()
            if (c != null && c.isConnected) c.disconnect()
        } catch (exception: IOException) {
            logger.info(exception.message)
        }
    }
}
