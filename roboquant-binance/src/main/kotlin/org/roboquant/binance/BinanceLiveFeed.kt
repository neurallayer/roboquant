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

package org.roboquant.binance

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.event.TickerEvent
import com.binance.api.client.domain.market.CandlestickInterval
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.TimeSpan
import org.roboquant.feeds.*
import java.time.Instant

/**
 * Alias for Binance Candlestick Interval
 */
typealias Interval = CandlestickInterval

/**
 * Create a new feed based on live price actions coming from the Binance exchange.
 *
 * @property useMachineTime use the machine time as timestamp for the generated events
 * @param configure additional configuration
 *
 * @constructor create a new instance of BinanceLiveFeed
 */
class BinanceLiveFeed(
    private val useMachineTime: Boolean = true,
    configure: BinanceConfig.() -> Unit = {}
) : LiveFeed(), AssetFeed {

    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger(BinanceLiveFeed::class)
    private val closeables = mutableListOf<AutoCloseable>()
    private val config = BinanceConfig()
    private val factory: BinanceApiClientFactory
    private val client: BinanceApiWebSocketClient
    private val assetMap: Map<String, Asset>

    /**
     * Get all available assets that can be subscribed to, including their symbols
     */
    val availableAssets
        get() = assetMap.values

    /**
     * Get the assets that have been subscribed to
     */
    override val assets
        get() = subscriptions.values.toSortedSet()

    init {
        config.configure()
        factory = Binance.getFactory(config)
        client = factory.newWebSocketClient()
        assetMap = Binance.retrieveAssets(factory.newRestClient())
        logger.debug { "started BinanceLiveFeed using web-socket client" }
    }

    private fun registerSymbols(symbols: Array<out String>) {
        require(symbols.isNotEmpty()) { "You need to provide at least 1 symbol" }
        for (symbol in symbols) require(assetMap.contains(symbol)) { "unknown symbol $symbol" }
        for (symbol in symbols) {
            val asset = assetMap.getValue(symbol)
            subscriptions[symbol] = asset
        }
    }

    /**
     * Subscribe to the price-bars for one or more [symbols]. Optional provide what the [interval] should be for
     * the price-bars, default is 1 minute.
     *
     * @see availableAssets
     */
    fun subscribePriceBar(
        vararg symbols: String,
        interval: Interval = Interval.ONE_MINUTE
    ) {
        registerSymbols(symbols)
        val subscription = symbols.joinToString(",") { it.lowercase() }
        val timeSpan = Binance.interval2TimeSpan(interval)
        val closable = client.onCandlestickEvent(subscription, interval) {
            handle(it, timeSpan)
        }
        closeables.add(closable)
        logger.info { "subscribed to $interval price-bars for $subscription" }
    }

    /**
     * Subscribe to the price-quotes for one or more [symbols].
     *
     * @see availableAssets
     */
    fun subscribePriceQuote(
        vararg symbols: String,
    ) {
        registerSymbols(symbols)
        val subscription = symbols.joinToString(",") { it.lowercase() }
        val closable = client.onTickerEvent(subscription) {
            handle(it)
        }
        closeables.add(closable)
        logger.info { "subscribed to price-quotes for $subscription" }
    }

    private fun handle(resp: TickerEvent) {
        val asset = subscriptions[resp.symbol]
        if (asset != null) {
            val action = PriceQuote(
                asset,
                resp.bestAskPrice.toDouble(),
                resp.bestAskQuantity.toDouble(),
                resp.bestBidPrice.toDouble(),
                resp.bestBidQuantity.toDouble()
            )

            val now = if (useMachineTime) Instant.now() else Instant.ofEpochMilli(resp.eventTime)
            val event = Event(now, listOf(action))
            send(event)
        } else {
            logger.warn { "Received TickerEvent for unsubscribed symbol ${resp.symbol}" }
        }

    }


    private fun handle(resp: CandlestickEvent, timeSpan: TimeSpan?) {
        if (!resp.barFinal) return

        logger.trace { "Received candlestick event for symbol ${resp.symbol}" }

        val asset = subscriptions[resp.symbol]
        if (asset != null) {
            val action = PriceBar(
                asset,
                resp.open.toDouble(),
                resp.high.toDouble(),
                resp.low.toDouble(),
                resp.close.toDouble(),
                resp.volume.toDouble(),
                timeSpan
            )
            val now = if (useMachineTime) Instant.now() else Instant.ofEpochMilli(resp.closeTime)
            val event = Event(now, listOf(action))
            send(event)
        } else {
            logger.warn { "Received CandlestickEvent for unsubscribed symbol ${resp.symbol}" }
        }
    }

    /**
     * Close this feed and stop receiving market data
     */
    @Suppress("TooGenericExceptionCaught")
    override fun close() {
        for (c in closeables) try {
            c.close()
        } catch (e: Throwable) {
            logger.debug(e) { "error during closing feed" }
        }
        closeables.clear()
    }

}

