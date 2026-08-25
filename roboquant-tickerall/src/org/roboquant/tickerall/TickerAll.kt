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

import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Currency
import org.roboquant.common.Forex
import org.roboquant.common.Logging
import java.util.concurrent.ConcurrentHashMap

/**
 * Configuration for connecting to a broker account hosted on TickerAll (https://tickerall.com), a hosted
 * MetaTrader 4 and 5 API. Every property can be supplied through the `configure` lambda on the
 * [TickerAllBroker], [TickerAllLiveFeed] and [TickerAllHistoricFeed] constructors, or through environment
 * variables / system properties (see [Config]). For example [apiKey] resolves from the `tickerall.api.key`
 * property or the `TICKERALL_API_KEY` environment variable.
 *
 * Everything on TickerAll is keyed by [accountId] — the id of a broker account that has already been
 * connected (warmed) on TickerAll. There are two ways to obtain it:
 *  - connect the account once from your MetaTrader credentials with [TickerAllBroker.connect], which starts
 *    the session, returns a broker bound to the resulting [accountId], and exposes it as
 *    [TickerAllBroker.accountId] (the recommended path, see [broker], [server], [account], [password]), or
 *  - reuse an [accountId] you already connected earlier (e.g. from the TickerAll dashboard or a prior
 *    [TickerAllBroker.connect]) and set it directly.
 *
 * @property apiKey the TickerAll API key, sent as a Bearer token (property name is tickerall.api.key)
 * @property accountId the id of the connected broker account to trade and stream (tickerall.account.id)
 * @property baseUrl the REST base url, default is https://api.tickerall.com (tickerall.base.url)
 * @property wsUrl the websocket stream url, default is wss://api.tickerall.com/v1/stream (tickerall.ws.url)
 * @property broker the MetaTrader platform to connect, either `"mt4"` or `"mt5"`; only used by
 * [TickerAllBroker.connect] to start a session (tickerall.broker)
 * @property server the broker server / trade server name, e.g. `"Exness-MT5Trial7"`; only used by
 * [TickerAllBroker.connect] (tickerall.server)
 * @property account the numeric broker login to connect; only used by [TickerAllBroker.connect]
 * (tickerall.account)
 * @property password the account password used to establish the session; only used by
 * [TickerAllBroker.connect] and never stored beyond the session (tickerall.password)
 * @property terminalType which client the connection presents as: `"MOBILE"` (default when blank), `"WEB"`
 * or `"CLIENT"`; only used by [TickerAllBroker.connect] (tickerall.terminal.type)
 *
 * @constructor Create a new instance of TickerAllConfig
 */
data class TickerAllConfig(
    var apiKey: String = Config.getProperty("tickerall.api.key", ""),
    var accountId: String = Config.getProperty("tickerall.account.id", ""),
    var baseUrl: String = Config.getProperty("tickerall.base.url", "https://api.tickerall.com"),
    var wsUrl: String = Config.getProperty("tickerall.ws.url", "wss://api.tickerall.com/v1/stream"),
    var broker: String = Config.getProperty("tickerall.broker", ""),
    var server: String = Config.getProperty("tickerall.server", ""),
    var account: String = Config.getProperty("tickerall.account", ""),
    var password: String = Config.getProperty("tickerall.password", ""),
    var terminalType: String = Config.getProperty("tickerall.terminal.type", ""),
)

/**
 * Logic shared between the TickerAll broker and feeds.
 */
internal object TickerAll {

    /**
     * Create and validate a [TickerAllClient] for the provided [config].
     */
    internal fun getClient(config: TickerAllConfig): TickerAllClient {
        require(config.apiKey.isNotBlank()) { "no api key provided (tickerall.api.key / TICKERALL_API_KEY)" }
        require(config.accountId.isNotBlank()) { "no account id provided (tickerall.account.id / TICKERALL_ACCOUNT_ID)" }
        // A broker account NUMBER (all digits) is a common mix-up for the TickerAll account id (a cuid) —
        // fail fast with the fix rather than a later, opaque "Broker account not found".
        require(!config.accountId.all { it.isDigit() }) {
            "accountId='${config.accountId}' looks like a broker account NUMBER, not a TickerAll account id " +
                "(a TickerAll id is a cuid, not a number). Use TickerAllBroker.connect { ... } with your " +
                "MetaTrader credentials, or set accountId to the id returned by TickerAll.startSession(...)."
        }
        return TickerAllClient(config)
    }

    /**
     * Start a broker session from the MetaTrader credentials in [config] (`broker`, `server`, `account`,
     * `password` and optional `terminalType`) by posting to `/v1/sessions`, and return the resulting
     * TickerAll `accountId`. An [accountId][TickerAllConfig.accountId] is not required here (this is the call
     * that produces it); the api key is. This mirrors the official SDKs' `sessions.start`.
     */
    internal fun startSession(config: TickerAllConfig): String {
        require(config.apiKey.isNotBlank()) { "no api key provided (tickerall.api.key / TICKERALL_API_KEY)" }
        require(config.broker.isNotBlank()) { "no broker provided; set broker to \"mt4\" or \"mt5\" (tickerall.broker)" }
        require(config.server.isNotBlank()) { "no server provided, e.g. \"Exness-MT5Trial7\" (tickerall.server)" }
        require(config.account.isNotBlank()) { "no account (numeric broker login) provided (tickerall.account)" }
        require(config.password.isNotBlank()) { "no password provided (tickerall.password)" }
        val body = SessionStartBody(
            broker = config.broker,
            server = config.server,
            account = config.account,
            password = config.password,
            // Blank means "not chosen": omit terminalType so the default MOBILE path is byte-unchanged.
            terminalType = config.terminalType.ifBlank { null },
        )
        // The /v1/sessions call is not account-scoped, so a client without an accountId is fine here.
        TickerAllClient(config).use { client ->
            val accountId = client.startSession(body).accountId
            require(!accountId.isNullOrBlank()) { "session start did not return an accountId" }
            return accountId
        }
    }

    private val assetCache = ConcurrentHashMap<String, Asset>()

    /**
     * Map a raw broker symbol (for example `EURUSD`, `XAUUSDm`, `BTCUSD`) to a stable roboquant [Asset].
     *
     * The raw broker symbol is always preserved as the asset symbol, so orders and prices refer to exactly
     * the instrument the broker knows. MetaTrader instruments (FX, metals, indices and crypto CFDs) are all
     * represented as a [Forex] asset so a single symbol maps consistently across the broker and feeds. The
     * asset currency is the instrument's quote currency: [quoteCurrency] when the caller resolved it from the
     * broker's symbol metadata (authoritative; see [SymbolCurrency]), otherwise it is inferred from the
     * trailing three letters of a standard pair, and only when the symbol is not a recognizable pair does it
     * fall back to [fallbackCurrency].
     */
    internal fun toAsset(symbol: String, fallbackCurrency: Currency, quoteCurrency: Currency? = null): Asset {
        // An asset's identity includes its currency, so the resolved currency is part of the cache key; the
        // broker and feeds pass the same metadata-resolved currency, so they share the cached instance.
        val key = "$symbol|${quoteCurrency?.currencyCode ?: ""}|${fallbackCurrency.currencyCode}"
        return assetCache.computeIfAbsent(key) {
            if (quoteCurrency != null) {
                Forex(symbol, quoteCurrency)
            } else {
                val letters = symbol.filter { it in 'A'..'Z' }
                val currency = if (letters.length >= 6) Currency.getInstance(letters.takeLast(3)) else fallbackCurrency
                Forex(symbol, currency)
            }
        }
    }
}

/**
 * Resolves a symbol's quote currency from the broker's symbol metadata (its profit currency), lazily and
 * cached. An asset's identity includes its currency, so the broker and the feeds share this resolution to
 * denote a symbol in the same currency — otherwise a position and its price events would be different assets.
 * Returns null when the symbol has no metadata currency (e.g. an MT4 account whose spec list is empty),
 * leaving [TickerAll.toAsset] to infer it from the pair.
 */
internal class SymbolCurrency(private val client: TickerAllClient) {

    private val logger = Logging.getLogger(SymbolCurrency::class)
    private var cache: Map<String, Currency>? = null

    fun get(symbol: String): Currency? {
        if (cache == null) {
            cache = try {
                client.getSymbolSpecs()
                    .filter { !it.name.isNullOrBlank() && !it.profitCurrency.isNullOrBlank() }
                    .associate { it.name!! to Currency.getInstance(it.profitCurrency!!) }
            } catch (e: Exception) {
                logger.warn { "failed to load symbol specs for currency resolution: ${e.message}" }
                emptyMap()
            }
        }
        return cache?.get(symbol)
    }
}
