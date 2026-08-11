/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.tickerall

import com.google.gson.Gson
import org.roboquant.common.Logging
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.WebSocket
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletionStage

/**
 * Exception thrown when the TickerAll API returns a non-success response.
 */
class TickerAllException(message: String) : Exception(message)

// --- Response DTOs. Fields are nullable so that a key absent from the JSON stays null (gson bypasses the
// --- Kotlin constructor, so default values are not applied); consumers decide the fallback explicitly.

internal data class AccountDTO(
    val account: AccountFinancialsDTO? = null,
    val positions: List<PositionDTO>? = null,
    val status: String? = null,
    val isDemo: Boolean? = null,
)

internal data class AccountFinancialsDTO(
    val balance: Double? = null,
    val currency: String? = null,
    val equity: Double? = null,
    val margin: Double? = null,
    val freeMargin: Double? = null,
    val marginLevel: Double? = null,
    val leverage: Int? = null,
)

internal data class PositionDTO(
    val ticket: Long? = null,
    val symbol: String? = null,
    val side: String? = null,
    val volume: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val entryPrice: Double? = null,
    val currentPrice: Double? = null,
    val profit: Double? = null,
    val openTime: String? = null,
)

internal data class PendingOrdersDTO(val orders: List<PendingOrderDTO>? = null)

internal data class PendingOrderDTO(
    val ticket: Long? = null,
    val symbol: String? = null,
    val side: String? = null,
    val orderType: String? = null,
    val volume: Double? = null,
    val price: Double? = null,
    val limitPrice: Double? = null,
)

internal data class OrderAckDTO(
    val ticket: Long? = null,
    val symbol: String? = null,
    val side: String? = null,
    val type: String? = null,
    val volume: Double? = null,
    val status: String? = null,
    val timestamp: String? = null,
    val price: Double? = null,
)

internal data class CandlesDTO(val candles: List<CandleDTO>? = null)

internal data class CandleDTO(
    val timestamp: Long? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val bid: Double? = null,
    val tickVolume: Long? = null,
    val spread: Double? = null,
)

internal data class SymbolsDTO(val symbols: List<String>? = null)

internal data class TickDTO(
    val type: String? = null,
    val accountId: String? = null,
    val symbol: String? = null,
    val bid: Double? = null,
    val ask: Double? = null,
    val timestamp: String? = null,
)

internal data class SessionStartResultDTO(
    val accountId: String? = null,
    val isDemo: Boolean? = null,
    val status: String? = null,
    val expiresAt: String? = null,
)

// --- Request bodies (gson omits null fields on serialization).

internal data class PlaceOrderBody(
    val type: String,
    val symbol: String,
    val side: String,
    val volume: Double,
    val price: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val comment: String? = null,
)

internal data class ModifyPendingBody(
    val price: Double? = null,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
)

internal data class CloseBody(val volume: Double? = null)

internal data class SessionStartBody(
    val broker: String,
    val server: String,
    val account: String,
    val password: String,
    val terminalType: String? = null,
)

/**
 * Minimal REST + WebSocket client for the TickerAll hosted MetaTrader API. It uses only the JDK
 * [HttpClient] / [WebSocket] (no third-party HTTP dependency) plus gson for JSON. All requests are
 * authenticated with the configured api key as a Bearer token, exactly like the official TickerAll SDKs.
 */
internal class TickerAllClient(private val config: TickerAllConfig) : AutoCloseable {

    private val logger = Logging.getLogger(TickerAllClient::class)
    private val gson = Gson()
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
    private val base = config.baseUrl.trimEnd('/')
    private val aid = enc(config.accountId)

    private fun enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)

    private fun request(method: String, path: String, body: Any?): HttpRequest {
        val builder = HttpRequest.newBuilder(URI.create(base + path))
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(60))
        // Any state-changing request needs an Idempotency-Key; the API requires it on POST/DELETE/PATCH.
        if (method != "GET") builder.header("Idempotency-Key", UUID.randomUUID().toString())
        when {
            body != null -> {
                builder.header("Content-Type", "application/json")
                builder.method(method, HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
            }
            method == "GET" -> builder.GET()
            else -> builder.method(method, HttpRequest.BodyPublishers.noBody())
        }
        return builder.build()
    }

    private fun exec(method: String, path: String, body: Any?): String {
        val resp = http.send(request(method, path, body), HttpResponse.BodyHandlers.ofString())
        if (resp.statusCode() !in 200..299) {
            throw TickerAllException("TickerAll $method $path -> HTTP ${resp.statusCode()}: ${resp.body()}")
        }
        return resp.body()
    }

    private fun <T> get(path: String, cls: Class<T>): T = gson.fromJson(exec("GET", path, null), cls)

    fun getAccount(): AccountDTO = get("/v1/accounts/$aid", AccountDTO::class.java)

    fun getPendingOrders(): List<PendingOrderDTO> =
        get("/v1/accounts/$aid/orders/pending", PendingOrdersDTO::class.java).orders ?: emptyList()

    fun getSymbols(): List<String> =
        get("/v1/accounts/$aid/symbols", SymbolsDTO::class.java).symbols ?: emptyList()

    fun getCandles(symbol: String, hours: Int, timeframe: String): List<CandleDTO> {
        val path = "/v1/accounts/$aid/candles?symbol=${enc(symbol)}&hours=$hours&timeframe=${enc(timeframe)}"
        return get(path, CandlesDTO::class.java).candles ?: emptyList()
    }

    /**
     * Start a broker session (`POST /v1/sessions`) and return the parsed result, whose `accountId` is the
     * connected account to use for every subsequent call. This endpoint is not account-scoped, so it works
     * on a client that has no [TickerAllConfig.accountId] yet.
     */
    fun startSession(body: SessionStartBody): SessionStartResultDTO =
        gson.fromJson(exec("POST", "/v1/sessions", body), SessionStartResultDTO::class.java)

    fun placeOrder(body: PlaceOrderBody): OrderAckDTO =
        gson.fromJson(exec("POST", "/v1/accounts/$aid/orders", body), OrderAckDTO::class.java)

    fun modifyPending(ticket: String, body: ModifyPendingBody) {
        exec("PATCH", "/v1/accounts/$aid/orders/${enc(ticket)}", body)
    }

    fun cancelPending(ticket: String) {
        exec("DELETE", "/v1/accounts/$aid/orders/${enc(ticket)}", null)
    }

    fun closePosition(ticket: String, volume: Double? = null) {
        exec("DELETE", "/v1/accounts/$aid/positions/${enc(ticket)}", CloseBody(volume))
    }

    /**
     * Open the tick websocket, subscribe to [symbols] and invoke [onTick] for every tick frame. The
     * returned [AutoCloseable] closes the underlying websocket.
     */
    fun streamTicks(symbols: Set<String>, onTick: (TickDTO) -> Unit): AutoCloseable {
        val listener = object : WebSocket.Listener {
            private val buffer = StringBuilder()
            override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                buffer.append(data)
                if (last) {
                    val msg = buffer.toString()
                    buffer.setLength(0)
                    try {
                        val tick = gson.fromJson(msg, TickDTO::class.java)
                        if (tick != null && tick.type == "tick" && tick.symbol != null) onTick(tick)
                    } catch (e: Exception) {
                        logger.trace { "ignored non-tick frame: ${e.message}" }
                    }
                }
                webSocket.request(1)
                return null
            }

            override fun onError(webSocket: WebSocket, error: Throwable) {
                logger.warn { "websocket error: ${error.message}" }
            }
        }
        val ws = http.newWebSocketBuilder()
            .header("Authorization", "Bearer ${config.apiKey}")
            .buildAsync(URI.create(config.wsUrl), listener)
            .join()
        val subscribe = gson.toJson(
            mapOf(
                "type" to "subscribe",
                "channels" to listOf(
                    mapOf("kind" to "ticks", "accountId" to config.accountId, "symbols" to symbols.toList())
                )
            )
        )
        ws.sendText(subscribe, true)
        return AutoCloseable {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "closing")
            } catch (e: Exception) {
                logger.trace { "error closing websocket: ${e.message}" }
            }
        }
    }

    override fun close() {
        // The JDK HttpClient does not require an explicit close, there is nothing to release here.
    }
}
