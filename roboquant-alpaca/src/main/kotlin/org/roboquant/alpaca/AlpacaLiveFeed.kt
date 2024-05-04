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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.bar.StockBarMessage
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.quote.StockQuoteMessage
import net.jacobpeterson.alpaca.model.websocket.marketdata.streams.stock.model.trade.StockTradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataListenerAdapter
import net.jacobpeterson.alpaca.websocket.marketdata.streams.stock.StockMarketDataWebsocketInterface
import org.roboquant.common.Asset
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.io.IOException
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Different types of price action that can be subscribed to
 */
enum class PriceActionType {

    /**
     * PriceBar type
     */
    PRICE_BAR,

    /**
     * Quote type
     */
    QUOTE,

    /**
     * Trade type
     */
    TRADE
}

/**
 * Alpaca feed allows you to subscribe to live market data from Alpaca. Alpaca needs a key and secret to access
 * their API. This live feed supports both stocks and crypto asset classes.
 *
 * You can provide these to the constructor or set them as environment variables.
 *
 * @constructor
 *
 */
class AlpacaLiveFeed(
    configure: AlpacaConfig.() -> Unit = {}
) : LiveFeed() {

    private val config = AlpacaConfig()
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaLiveFeed::class)
    private val listener = createStockHandler()


    init {
        config.configure()
        alpacaAPI = Alpaca.getAPI(config)
        connect()
    }



    /**
     * Connect to streaming data for the stock market and crypto market
     */
    private fun connect() {
        connectMarket(alpacaAPI.stockMarketDataStream())
        // connectMarket(alpacaAPI.cryptoMarketDataStream())
    }

    /**
     * Connect to ta market data provider and start listening. This can be the stocks or crypto market data feeds.
     */
    private fun connectMarket(connection: StockMarketDataWebsocketInterface) {
        require(!connection.isConnected) { "already connected, disconnect first" }
        val timeoutMillis: Long = 5_000
        connection.setAutomaticallyReconnect(true)
        connection.connect()
        connection.waitForAuthorization(timeoutMillis, TimeUnit.MILLISECONDS)
        if (!connection.isValid) {
            throw ConfigurationException("couldn't establish $connection")
        } else {
            connection.setListener(listener)
        }
    }

    /**
     * Stop listening for market data
     */
    override fun close() {
        try {
            if (alpacaAPI.stockMarketDataStream().isConnected) alpacaAPI.stockMarketDataStream().disconnect()
            if (alpacaAPI.cryptoMarketDataStream().isConnected) alpacaAPI.cryptoMarketDataStream().disconnect()
        } catch (exception: IOException) {
            logger.info(exception.message)
        }
    }

    /**
     * Just cleanup any used connection
     */
    fun finalize() {
        close()
    }

    /**
     * Subscribe to stock market data based on the passed [symbols] and [type]
     */
    fun subscribeStocks(vararg symbols: String, type: PriceActionType = PriceActionType.PRICE_BAR) {
        // validateSymbols(symbols, availableStocksMap)
        val s = symbols.toList()
        when (type) {
            PriceActionType.TRADE -> alpacaAPI.stockMarketDataStream().tradeSubscriptions.addAll(s)
            PriceActionType.QUOTE -> alpacaAPI.stockMarketDataStream().quoteSubscriptions.addAll(s)
            PriceActionType.PRICE_BAR -> alpacaAPI.stockMarketDataStream().minuteBarSubscriptions.addAll(s)
        }
    }

    /**
     * Subscribe to crypto market data b based on the passed [symbols] and [type]
     */
    fun subscribeCrypto(vararg symbols: String, type: PriceActionType = PriceActionType.PRICE_BAR) {
        // validateSymbols(symbols, availableCryptoMap)
        val s = symbols.toList()
        when (type) {
            PriceActionType.TRADE -> alpacaAPI.cryptoMarketDataStream().tradeSubscriptions.addAll(s)
            PriceActionType.QUOTE -> alpacaAPI.cryptoMarketDataStream().quoteSubscriptions.addAll(s)
            PriceActionType.PRICE_BAR -> alpacaAPI.cryptoMarketDataStream().minuteBarSubscriptions.addAll(s)
        }
    }

    private fun send(time: Instant, item: PriceItem) {
        logger.trace { "received item=$item time=$time" }
        val event = Event(time, listOf(item))
        send(event)
    }

    private fun createStockHandler(): StockMarketDataListenerAdapter {
        return object : StockMarketDataListenerAdapter() {

            override fun onTrade(trade: StockTradeMessage) {
                val asset = Asset(trade.symbol)
                val item = TradePrice(asset, trade.price)
                val time = trade.timestamp.toInstant()
                send(time, item)
            }

            override fun onQuote(quote: StockQuoteMessage) {
                val asset = Asset(quote.symbol)
                val item = PriceQuote(
                    asset,
                    quote.askPrice,
                    quote.askSize.toDouble(),
                    quote.bidPrice,
                    quote.bidSize.toDouble(),
                )
                val time = quote.timestamp.toInstant()
                send(time, item)
            }

            override fun onMinuteBar(bar: StockBarMessage) {
                val asset = Asset(bar.symbol)
                val item = PriceBar(
                    asset,
                    bar.open,
                    bar.high,
                    bar.low,
                    bar.close,
                    bar.tradeCount.toDouble()
                )
                val time = bar.timestamp.toInstant()
                send(time, item)
            }


        }
    }
}

