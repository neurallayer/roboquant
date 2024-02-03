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

package org.roboquant.tiingo

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.collections.set


private val logger = Logging.getLogger(TiingoLiveFeed::class)

private class Handler(private val feed: TiingoLiveFeed) : WebSocketListener() {

    private fun handleIEX(data: JsonArray) {
        val type = data[0].asString
        val symbol = data[3].asString.uppercase()
        val asset = Config.getOrPutAsset(symbol) { Asset(symbol) }
        val time = Instant.ofEpochMilli(0).plusNanos(data[2].asLong)

        if (type == "Q") {
            val quote = PriceQuote(asset, data[7].asDouble, data[8].asDouble, data[5].asDouble, data[4].asDouble)
            feed.deliver(quote, time)
        } else if (type == "T") {
            val trade = TradePrice(asset, data[9].asDouble, data[10].asDouble)
            feed.deliver(trade, time)
        }
    }

    private fun handleFX(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[1].asString.uppercase()
            val asset = Config.getOrPutAsset(symbol) { Asset.forexPair(symbol) }
            val quote = PriceQuote(asset, data[7].asDouble, data[6].asDouble, data[4].asDouble, data[3].asDouble)
            val time = Instant.parse(data[2].asString)
            feed.deliver(quote, time)
        }
    }

    private fun handleCrypto(data: JsonArray) {
        val type = data[0].asString
        val symbol = data[1].asString.uppercase()
        val asset = Config.getOrPutAsset(symbol) { Asset(symbol, AssetType.CRYPTO, exchange = Exchange.CRYPTO) }
        val time = Instant.parse(data[2].asString)
        if (type == "Q") {
            val quote = PriceQuote(asset, data[8].asDouble, data[7].asDouble, data[5].asDouble, data[4].asDouble)
            feed.deliver(quote, time)
        } else if (type == "T") {
            val trade = TradePrice(asset, data[5].asDouble, data[4].asDouble)
            feed.deliver(trade, time)
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
 * Retrieve real-time data from Tiingo. It has support for retreiving US stocks, Forex and Crypto.
 *
 * This feed uses the Tiingo websocket API for low letency and has nanosecond time resolution.
 */
class TiingoLiveFeed private constructor(
    private val type: String,
    private val thresholdLevel: Int = 5,
    configure: TiingoConfig.() -> Unit = {}
) : LiveFeed() {

    private val config = TiingoConfig()

    init {
        val types = setOf("iex", "crypto", "fx")
        require(type in types) { "invalid type $type, allowed types are $types"}
        config.configure()
        require(config.key.isNotBlank()) { "no valid key found"}
    }

    companion object {

        fun iex(thresholdLevel: Int = 5, configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("iex", thresholdLevel, configure)
        }

        fun crypto(thresholdLevel: Int = 2, configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("crypto", thresholdLevel, configure)
        }

        fun fx(thresholdLevel: Int = 5, configure: TiingoConfig.() -> Unit = {}): TiingoLiveFeed {
            return TiingoLiveFeed("fx", thresholdLevel, configure)
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
     * Close the websocket connection with Tiingo
     */
    override fun close() {
        when (type) {
            "iex" -> wsIEX.close(1000, "closed called")
            "fx" -> wsFX.close(1000, "closed called")
            "crypto" -> wsCrypto.close(1000, "closed called")
        }
    }

    /**
     * Subscribe to real-time trades/quotes for provided [symbols].
     *
     * If no symbols are provided, all available market data is subscribed to. Be aware that this a lot of data.
     *
     * For crypto and some forex it is challenging to derive the underlying [Asset] from just its symbol name.
     * You can use [Config.registerAsset] to register assets upfront
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


}
