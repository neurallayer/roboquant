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
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceQuote
import java.time.Instant
import java.util.concurrent.TimeUnit


private val logger = Logging.getLogger(TiingoLiveFeed::class)

private class Handler(private val feed: TiingoLiveFeed) : WebSocketListener() {

    private val stocks = mutableMapOf<String, Asset>()
    private val fx = mutableMapOf<String, Asset>()

    private fun handleIEX(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[3].asString
            val asset = stocks.getOrPut(symbol) { Asset(symbol.uppercase()) }
            val quote = PriceQuote(asset, data[7].asDouble, data[8].asDouble, data[5].asDouble, data[4].asDouble)
            val time = Instant.ofEpochMilli(0).plusNanos(data[2].asLong)
            feed.deliver(quote, time)
        }
    }

    private fun handleFX(data: JsonArray) {
        val type = data[0].asString
        if (type == "Q") {
            val symbol = data[3].asString
            val asset = fx.getOrPut(symbol) { Asset(symbol.uppercase(), AssetType.FOREX) }
            val quote = PriceQuote(asset, data[7].asDouble, data[8].asDouble, data[4].asDouble, data[3].asDouble)
            val time = Instant.parse(data[2].asString)
            feed.deliver(quote, time)
        }
    }


    @Suppress("TooGenericExceptionCaught")
    override fun onMessage(webSocket: WebSocket, text: String) {
        println(text)
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
class TiingoLiveFeed : LiveFeed() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val wsIEX: WebSocket by lazy {
        val request = Request.Builder()
            .url("wss://api.tiingo.com/iex")
            .build()
        logger.info { "Connecting to Tiingo" }
        client.newWebSocket(request, handler)
    }

    private val handler = Handler(this)
    private val key: String = Config.getProperty("tiingo.key") ?: throw ConfigurationException("cannot find tiingo.key")
    private val gsonBuilder = GsonBuilder()


    internal fun deliver(action: PriceAction, time: Instant) {
        val event = Event(listOf(action), time)
        send(event)
    }

    /**
     * Subscribe to IEX quotes for provided [symbols].
     *
     * If no symbols are provided, all available stock data is subscribed to. That is a lot of data and can impact
     * your monthly quato quickly.
     */
    fun subscribeIEX(vararg symbols: String) {

        val eventData = mutableMapOf<String, Any>("thresholdLevel" to 5)
        if (symbols.isNotEmpty()) eventData["tickers"] = symbols.toList()

        val message = mapOf(
           "eventName" to "subscribe",
           "authorization" to key,
           "eventData" to eventData
        )
        val json = gsonBuilder.create().toJson(message)
        wsIEX.send(json)
    }

}
