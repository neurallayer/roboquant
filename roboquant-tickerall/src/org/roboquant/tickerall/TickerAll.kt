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
import java.util.concurrent.ConcurrentHashMap

/**
 * Configuration for connecting to a broker account hosted on TickerAll (https://tickerall.com), a hosted
 * MetaTrader 4 and 5 API. Every property can be supplied through the `configure` lambda on the
 * [TickerAllBroker], [TickerAllLiveFeed] and [TickerAllHistoricFeed] constructors, or through environment
 * variables / system properties (see [Config]). For example [apiKey] resolves from the `tickerall.api.key`
 * property or the `TICKERALL_API_KEY` environment variable.
 *
 * @property apiKey the TickerAll API key, sent as a Bearer token (property name is tickerall.api.key)
 * @property accountId the id of the connected broker account to trade and stream (tickerall.account.id)
 * @property baseUrl the REST base url, default is https://api.tickerall.com (tickerall.base.url)
 * @property wsUrl the websocket stream url, default is wss://api.tickerall.com/v1/stream (tickerall.ws.url)
 *
 * @constructor Create a new instance of TickerAllConfig
 */
data class TickerAllConfig(
    var apiKey: String = Config.getProperty("tickerall.api.key", ""),
    var accountId: String = Config.getProperty("tickerall.account.id", ""),
    var baseUrl: String = Config.getProperty("tickerall.base.url", "https://api.tickerall.com"),
    var wsUrl: String = Config.getProperty("tickerall.ws.url", "wss://api.tickerall.com/v1/stream"),
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
        return TickerAllClient(config)
    }

    private val assetCache = ConcurrentHashMap<String, Asset>()

    /**
     * Map a raw broker symbol (for example `EURUSD`, `XAUUSDm`, `BTCUSD`) to a stable roboquant [Asset].
     *
     * The raw broker symbol is always preserved as the asset symbol, so orders and prices refer to exactly
     * the instrument the broker knows. MetaTrader instruments (FX, metals, indices and crypto CFDs) are all
     * represented as a [Forex] asset so a single symbol maps consistently across the broker and feeds. The
     * quote currency is derived from the trailing three letters of the symbol when it looks like a standard
     * pair, otherwise [fallbackCurrency] is used.
     */
    internal fun toAsset(symbol: String, fallbackCurrency: Currency): Asset = assetCache.computeIfAbsent(symbol) {
        val letters = symbol.filter { it in 'A'..'Z' }
        val currency = if (letters.length >= 6) Currency.getInstance(letters.takeLast(3)) else fallbackCurrency
        Forex(symbol, currency)
    }
}
