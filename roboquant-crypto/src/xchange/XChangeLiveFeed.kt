/*
 * Copyright 2021 Neural Layer
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
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.time.Instant
import org.knowm.xchange.dto.marketdata.OrderBook as CryptoOrderBook

/**
 * Get live-streaming feeds from dozens of bitcoin exchanges. This feed uses the XChange library
 * to connect to the different exchanges to get the trade and order book feeds for a currency pair.
 *
 * XChange is a Java library providing a streamlined API for interacting with 60+ Bitcoin and Altcoin exchanges
 * providing a consistent interface for trading and accessing market data.
 *
 * See also https://knowm.org/open-source/xchange/
 *
 * # Example usage
 *
 *      val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange::class.java)
 *      exchange.connect().blockingAwait()
 *      val feed =  CryptoFeed(exchange)
 *
 * @property useMachineTime use the local machine time to stamp the event or the exchange provided timestamp.
 * @constructor
 *
 * @param exchange the exchange to use. The connection to the provided exchange should already be live.
 */
class XChangeLiveFeed(
    exchange: StreamingExchange,
    private val useMachineTime: Boolean = true
) : LiveFeed() {

    private val logger = Logging.getLogger(XChangeLiveFeed::class)
    private val service: StreamingMarketDataService = exchange.streamingMarketDataService
    private val subscriptions = mutableListOf<Disposable>()
    private val exchangeName = exchange.toString()

    /**
     * Assets that are subscribed to
     */
    val assets = mutableSetOf<Asset>()

    init {
        if (!exchange.isAlive) logger.warning { "Exchange connection is not yet live" }
        logger.info { "Establishing feed for exchange $exchangeName" }

        logger.fine {
            val pairs = exchange.exchangeMetaData.currencyPairs.keys
            "Available currency pairs $pairs"
        }

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
                { throwable -> logger.warning { "Error in trade subscription ${throwable.stackTrace}" } })

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
                    { throwable -> logger.warning { "Error in order book subscription ${throwable.stackTrace}" } })
            subscriptions.add(subscription)
            assets.add(asset)
        }
    }


    /**
     * Subscribe to live ticker updates from the exchange. The resulting events will contain
     * [PriceBar] actions (OHLCV). This is especially suited for technical analysis like candle stick
     * patterns.
     *
     */
    fun subscribeTicker(vararg currencyPairs: Pair<String, String>) {
        for (currencyPair in currencyPairs) {
            val cryptoPair = CurrencyPair(currencyPair.first, currencyPair.second)
            val asset = getAsset(cryptoPair)
            val subscription = service.getTicker(cryptoPair)
                .subscribe(
                    { ticker -> handleTicker(asset, ticker) },
                    { throwable -> logger.warning { "Error in ticker subscription ${throwable.stackTrace}" } })
            subscriptions.add(subscription)
            assets.add(asset)
        }
    }

    /**
     * Get an asset based on a crypto-currency pair.
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
     * no run yet started and as a result there is no active channel, the trade events will be ignored.
     *
     * @param asset
     * @param trade
     */
    private fun handleTrade(asset: Asset, trade: Trade) {
        logger.finer { "$trade event for $asset" }
        val item = TradePrice(asset, trade.price.toDouble(), trade.originalAmount.toDouble())
        val now = if (useMachineTime) Instant.now() else trade.timestamp.toInstant()
        val event = Event(listOf(item), now)
        channel?.offer(event)
    }

    /**
     * Process an order book callback and create [OrderBook] actions.
     *
     * @param asset
     * @param orderBook
     */
    private fun handleOrderBook(asset: Asset, orderBook: CryptoOrderBook) {
        logger.finer { "$orderBook event for $asset" }
        val asks =
            orderBook.asks.map { OrderBook.OrderBookEntry(it.cumulativeAmount.toDouble(), it.limitPrice.toDouble()) }
        val bids =
            orderBook.bids.map { OrderBook.OrderBookEntry(it.cumulativeAmount.toDouble(), it.limitPrice.toDouble()) }
        val item = OrderBook(asset, asks, bids)
        val now = if (useMachineTime) Instant.now() else orderBook.timeStamp.toInstant()
        val event = Event(listOf(item), now)
        channel?.offer(event)
    }

    /**
     * Process ticker callback and create [PriceBar] actions.
     *
     * @param asset
     * @param ticker
     */
    private fun handleTicker(asset: Asset, ticker: Ticker) {
        logger.finer { "$ticker event for $asset" }
        if (ticker.open == null) {
            logger.finer { "Received ticker for ${asset.symbol} without open value" }
            return
        }
        val item = PriceBar(asset, ticker.open, ticker.high, ticker.low, ticker.last, ticker.volume)
        val now = if (useMachineTime) Instant.now() else ticker.timestamp.toInstant()
        val event = Event(listOf(item), now)
        channel?.offer(event)
    }


}