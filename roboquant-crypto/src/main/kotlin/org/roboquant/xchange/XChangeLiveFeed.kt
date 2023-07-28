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

package org.roboquant.xchange

import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingMarketDataService
import io.reactivex.disposables.Disposable
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import org.knowm.xchange.dto.marketdata.Trade
import org.roboquant.common.*
import org.roboquant.common.Currency
import org.roboquant.feeds.*
import java.time.Instant
import java.util.*
import org.knowm.xchange.dto.marketdata.OrderBook as CryptoOrderBook

/**
 * Get live-streaming feeds from dozens of bitcoin exchanges. This feed uses the XChange library
 * to connect to the different exchanges to get the trade and order book feeds for a currency pair.
 *
 * XChange is a Java library providing a streamlined API for interacting with 60+ Bitcoin and Altcoin exchanges,
 * providing a consistent interface for trading and accessing market data.
 *
 * See also https://knowm.org/open-source/xchange/
 *
 * # Example usage
 * ```
 *  val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange::class.java)
 *  exchange.connect().blockingAwait()
 *  val feed = CryptoFeed(exchange)
 * ```
 * @property useMachineTime use the local machine time to stamp the event or the exchange provided timestamp.
 * @constructor
 *
 * @param exchange the exchange to use. The connection to the provided exchange should already be live.
 */
class XChangeLiveFeed(
    exchange: StreamingExchange,
    private val useMachineTime: Boolean = true
) : LiveFeed(), AssetFeed {

    private val logger = Logging.getLogger(XChangeLiveFeed::class)
    private val service: StreamingMarketDataService = exchange.streamingMarketDataService
    private val subscriptions = mutableListOf<Disposable>()
    private val exchangeName = exchange.exchangeSpecification?.exchangeName ?: exchange.toString()

    /**
     * Assets that are available to subscribe to
     */
    val availableAssets by lazy {
        val meta = exchange.exchangeInstruments
        if (meta == null) {
            logger.info("No metadata available")
            emptyList<Asset>()
        } else {
            meta.map {
                val symbol = "${it.base.currencyCode}_${it.counter.currencyCode}"
                Asset(symbol, AssetType.CRYPTO, it.counter.currencyCode, exchangeName)
            }
        }
    }

    /**
     * Assets that are subscribed to
     */
    override val assets = TreeSet<Asset>()

    init {
        logger.info { "Establishing feed for exchange $exchangeName" }
        if (!exchange.isAlive) logger.warn { "Exchange connection is not yet live" }
    }

    /**
     * Subscribe to live trade updates from the exchange. The resulting actions will be of the
     * type of [TradePrice] events.
     *
     * @param currencyPairs
     */
    fun subscribeTrade(vararg currencyPairs: Pair<String, String>) {

        for (currencyPair in currencyPairs) {
            val cryptoPair = CurrencyPair(currencyPair.first, currencyPair.second)
            val asset = getAsset(cryptoPair)
            val subscription = service.getTrades(cryptoPair).subscribe(
                { trade -> handleTrade(asset, trade) },
                { throwable -> logger.warn(throwable) { "Error in trade subscription" } })

            subscriptions.add(subscription)
            assets.add(asset)
        }

    }

    /**
     * Subscribe to live order book updates from the exchange. The resulting actions will be of the
     * type of [OrderBook].
     *
     * @param currencyPairs
     */
    fun subscribeOrderBook(vararg currencyPairs: Pair<String, String>) {
        for (currencyPair in currencyPairs) {
            val cryptoPair = CurrencyPair(currencyPair.first, currencyPair.second)
            val asset = getAsset(cryptoPair)
            val subscription = service.getOrderBook(cryptoPair)
                .subscribe(
                    { orderBook -> handleOrderBook(asset, orderBook) },
                    { throwable -> logger.warn(throwable) { "Error in order book subscription" } })
            subscriptions.add(subscription)
            assets.add(asset)
        }
    }

    /**
     * Subscribe to live ticker updates from the exchange. The resulting events will contain
     * [PriceQuote] actions.
     */
    fun subscribeTicker(vararg symbols: String) {
        for (symbol in symbols) {

            val currencyPair = symbol.toCurrencyPair()
            if (currencyPair != null) {
                val asset = getAsset2(symbol, currencyPair.second)
                val cryptoPair = CurrencyPair(currencyPair.first.currencyCode, currencyPair.second.currencyCode)
                val subscription = service.getTicker(cryptoPair)
                    .subscribe(
                        { ticker -> handleTicker(asset, ticker) },
                        { throwable -> logger.warn(throwable) { "Error in ticker subscription" } }
                    )
                subscriptions.add(subscription)
                assets.add(asset)
            } else {
                logger.warn { "Error in converting $symbol to currency pair" }
            }
        }
    }

    /**
     * Get an asset based on a cryptocurrency pair.
     *
     * @return
     */
    private fun getAsset2(symbol: String, currency: Currency): Asset {
        return Asset(symbol, AssetType.CRYPTO, currency.currencyCode, exchangeName)
    }

    /**
     * Get an asset based on a cryptocurrency pair.
     *
     * @param currencyPair
     * @return
     */
    private fun getAsset(currencyPair: CurrencyPair): Asset {
        return Asset(
            symbol = currencyPair.base.currencyCode,
            currencyCode = currencyPair.counter.currencyCode,
            exchangeCode = exchangeName,
            type = AssetType.CRYPTO
        )
    }

    /**
     * Handle trade events and put them on the channel to be processed. In case there is
     * no run yet started, and as a result, there is no active channel, the trade events will be ignored.
     *
     * @param asset
     * @param trade
     */
    private fun handleTrade(asset: Asset, trade: Trade) {
        logger.trace { "$trade event for $asset" }
        val item = TradePrice(asset, trade.price.toDouble(), trade.originalAmount.toDouble())
        val now = if (useMachineTime) Instant.now() else trade.timestamp.toInstant()
        val event = Event(listOf(item), now)
        send(event)
    }

    /**
     * Process an order book callback and create [OrderBook] actions.
     *
     * @param asset
     * @param orderBook
     */
    private fun handleOrderBook(asset: Asset, orderBook: CryptoOrderBook) {
        logger.trace { "$orderBook event for $asset" }
        val asks = orderBook.asks.map {
            OrderBook.OrderBookEntry(it.cumulativeAmount.toDouble(), it.limitPrice.toDouble())
        }
        val bids = orderBook.bids.map {
            OrderBook.OrderBookEntry(it.cumulativeAmount.toDouble(), it.limitPrice.toDouble())
        }
        val item = OrderBook(asset, asks, bids)
        val now = if (useMachineTime) Instant.now() else orderBook.timeStamp.toInstant()
        val event = Event(listOf(item), now)
        send(event)
    }

    /**
     * Process ticker callback and create [PriceQuote] actions.
     *
     * @param asset
     * @param ticker
     */
    private fun handleTicker(asset: Asset, ticker: Ticker) {
        logger.trace { "$ticker event for $asset" }
        val item = PriceQuote(
            asset,
            ticker.ask.toDouble(),
            ticker.askSize.toDouble(),
            ticker.bid.toDouble(),
            ticker.bidSize.toDouble()
        )
        val now = if (useMachineTime) Instant.now() else ticker.timestamp.toInstant()
        val event = Event(listOf(item), now)
        send(event)
    }

}