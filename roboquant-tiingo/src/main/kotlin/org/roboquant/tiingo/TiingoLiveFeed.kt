/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.tiingo

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceQuote
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.collections.set


/**
 * Configuration for Tiingo connections
 *
 * @property key the Tiingo api key to use (property name is tiingo.key)
 */
data class TiingoConfig(
    var key: String = Config.getProperty("tiingo.key", ""),
)

private val logger = Logging.getLogger(TiingoLiveFeed::class)

private class Handler(private val feed: TiingoLiveFeed) : WebSocketListener() {

    val assets = mutableMapOf<String, Asset>()

    private fun handleIEX(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[3].asString.uppercase()
            val asset = assets.getOrPut(symbol) { Asset(symbol) }
            val quote = PriceQuote(asset, data[7].asDouble, data[8].asDouble, data[5].asDouble, data[4].asDouble)
            val time = Instant.ofEpochMilli(0).plusNanos(data[2].asLong)
            feed.deliver(quote, time)
        }
    }

    private fun handleFX(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[1].asString.uppercase()
            val asset = assets.getOrPut(symbol) { Asset.forexPair(symbol) }
            val quote = PriceQuote(asset, data[7].asDouble, data[6].asDouble, data[4].asDouble, data[3].asDouble)
            val time = Instant.parse(data[2].asString)
            feed.deliver(quote, time)
        }
    }

    private fun handleCrypto(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[1].asString.uppercase()
            val asset = assets.getOrPut(symbol) { Asset(symbol, AssetType.CRYPTO) }
            val quote = PriceQuote(asset, data[8].asDouble, data[7].asDouble, data[5].asDouble, data[4].asDouble)
            val time = Instant.parse(data[2].asString)
            feed.deliver(quote, time)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.trace { text }
        try {
            val root = JsonParser.parseString(text).asJsonObject
            val messageType = root["messageType"].asString
            if (messageType != "A") return

            val service = root["service"].asString
            val data = root["data"].asJsonArray

            when (service) {
                "iex" -> handleIEX(data)
                "fx" -> handleFX(data)
                "crypto_data" -> handleCrypto(data)
            }

        } catch (e: Throwable) {
            logger.warn("error handling tiingo web-socket message", e)
        }
    }

}

/**
 * Tiingo live feed
 *
 * This feed uses web-sockets for low letency and has nanosecond resolution
 */
class TiingoLiveFeed private constructor(
    private val type: String,
    private val thresholdLevel: Int,
    configure: TiingoConfig.() -> Unit = {}
) : LiveFeed() {

    private val config = TiingoConfig()

    init {
        config.configure()
        require(config.key.isNotBlank()) { "no valid key found"}
    }

    companion object {

        fun iex(configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("iex", 5, configure)
        }

        fun crypto(configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("crypto", 2, configure)
        }

        fun fx(configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("fx", 5, configure)
        }

    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()


    private fun getSocket(type: String): WebSocket {
        val request = Request.Builder()
            .url("wss://api.tiingo.com/$type")
            .build()
        logger.info { "Connecting to $type web-socket" }
        return client.newWebSocket(request, handler)
    }

    private val wsIEX: WebSocket by lazy {
        getSocket("iex")
    }

    private val wsFX: WebSocket by lazy {
        getSocket("fx")
    }

    private val wsCrypto: WebSocket by lazy {
        getSocket("crypto")
    }

    private val handler = Handler(this)
    private val gsonBuilder = GsonBuilder()

    @Synchronized
    internal fun deliver(action: PriceAction, time: Instant) {
        val event = Event(listOf(action), time)
        send(event)
    }

    /**
     * Close the connections with Tiingo
     */
    override fun close() {
        when (type) {
            "iex" -> wsIEX.close(0, "closed called")
            "fx" -> wsFX.close(0, "closed called")
            "crypto" -> wsCrypto.close(0, "closed called")
        }
    }

    /**
     * Subscribe to quotes for provided [symbols].
     *
     * If no symbols are provided, all available data is subscribed to. That is a lot of data and can impact
     * your monthly quato quickly.
     *
     * For crypto and some forex it is challenging to derive the underlying currency from just the symbol name,
     * so it is better to use [subscribeAssets] instead.
     */
    fun subscribe(vararg symbols: String) {

        val eventData = mutableMapOf<String, Any>("thresholdLevel" to thresholdLevel)
        if (symbols.isNotEmpty()) eventData["tickers"] = symbols.toList()

        val message = mapOf(
            "eventName" to "subscribe",
            "authorization" to config.key,
            "eventData" to eventData
        )

        val json = gsonBuilder.create().toJson(message)
        when (type) {
            "iex" -> wsIEX.send(json)
            "fx" -> wsFX.send(json)
            "crypto" -> wsCrypto.send(json)
        }

    }

    /**
     * Subscribe to quotes for the provided [assets].
     *
     * If no symbols are provided, all available data is subscribed to. That is a lot of data and can impact
     * your monthly quato quickly.
     */
    fun subscribeAssets(vararg assets: Asset) {
        val tickers = mutableListOf<String>()
        for (asset in assets) {
            val tiingoTicker = asset.symbol.replace("[^a-zA-Z]+".toRegex(), "")
            this.handler.assets[tiingoTicker] = asset
            tickers.add(tiingoTicker)
        }

        @Suppress("SpreadOperator")
        subscribe(*tickers.toTypedArray())
    }

}
