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

import org.roboquant.common.Currency
import org.roboquant.common.Event
import org.roboquant.common.Logging
import org.roboquant.common.PriceQuote
import org.roboquant.feeds.LiveFeed
import java.time.Instant

/**
 * Live feed that streams real-time bid/ask ticks for one connected MetaTrader account over the TickerAll
 * websocket. Each tick is published as a [PriceQuote], since MetaTrader ticks are bid/ask quotes. After
 * constructing the feed, call [subscribe] with the symbols you want to receive.
 *
 * The feed streams an already-connected account, so set the [accountId][TickerAllConfig.accountId] of one.
 * If you only have MetaTrader credentials, connect once with [TickerAllBroker.connect] and pass
 * `accountId = broker.accountId` here, so the broker and feed share a single session:
 * ```
 * val broker = TickerAllBroker.connect { apiKey = "..."; broker = "mt5"; server = "..."; account = "..."; password = "..." }
 * val feed = TickerAllLiveFeed { apiKey = "..."; accountId = broker.accountId }
 * ```
 *
 * @param configure configuration for connecting to the TickerAll API
 * @constructor Create a new instance of the TickerAllLiveFeed
 */
class TickerAllLiveFeed(
    configure: TickerAllConfig.() -> Unit = {}
) : LiveFeed() {

    private val config = TickerAllConfig()
    private val client: TickerAllClient
    private val logger = Logging.getLogger(TickerAllLiveFeed::class)
    private var subscription: AutoCloseable? = null

    init {
        config.configure()
        client = TickerAll.getClient(config)
    }

    /**
     * Subscribe to live ticks for the provided [symbols]. A single call replaces any previous subscription.
     */
    fun subscribe(vararg symbols: String) {
        require(symbols.isNotEmpty()) { "provide at least one symbol" }
        subscription?.close()
        subscription = client.streamTicks(symbols.toSet()) { tick ->
            val symbol = tick.symbol ?: return@streamTicks
            val asset = TickerAll.toAsset(symbol, Currency.USD)
            // MetaTrader ticks carry bid/ask but no sizes, so the quote sizes are reported as NaN (unknown).
            val item = PriceQuote(asset, tick.ask ?: Double.NaN, Double.NaN, tick.bid ?: Double.NaN, Double.NaN)
            send(Event(parseTime(tick.timestamp), listOf(item)))
        }
    }

    private fun parseTime(ts: String?): Instant {
        if (ts.isNullOrBlank()) return Instant.now()
        return try {
            Instant.parse(ts)
        } catch (e: Exception) {
            ts.toLongOrNull()?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
        }
    }

    /**
     * Stop streaming and close the underlying websocket.
     */
    override fun close() {
        try {
            subscription?.close()
        } catch (e: Exception) {
            logger.info(e.message)
        }
        subscription = null
        client.close()
    }
}
