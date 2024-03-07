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
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.MarketDataMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.SymbolMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.bar.BarMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.control.ErrorMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.enums.MarketDataMessageType
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.quote.QuoteMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.trade.TradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataWebsocketInterface
import org.roboquant.common.Asset
import org.roboquant.common.ConfigurationException
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.io.IOException
import java.time.Instant
import java.util.*
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
) : LiveFeed(), AssetFeed {

    private val config = AlpacaConfig()
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaLiveFeed::class)
    private val listener = createListener()
    private val subscribedSymbols = mutableListOf<String>()

    private val assetMap by lazy {
        availableAssets.associateBy { it.symbol }
    }

    init {
        config.configure()
        alpacaAPI = Alpaca.getAPI(config)
        connect()
    }

    private val availableStocksMap: Map<String, Asset> by lazy {
        Alpaca.getAvailableStocks(alpacaAPI)
    }

    private val availableCryptoMap: Map<String, Asset> by lazy {
        Alpaca.getAvailableCrypto(alpacaAPI)
    }

    /**
     * Returns the available crypto to subscribe to
     */
    val availableCrypto: SortedSet<Asset>
        get() = availableCryptoMap.values.toSortedSet()

    /**
     * Returns the available stocks to subscribe to
     */
    val availableStocks: SortedSet<Asset>
        get() = availableStocksMap.values.toSortedSet()

    /**
     * Returns all available assets to subscribe to, stocks and crypto combined
     */
    val availableAssets: SortedSet<Asset>
        get() = (availableStocksMap.values + availableCryptoMap.values).toSortedSet()

    /**
     * Returns subscribed assets, stocks and crypto combined
     */
    override val assets: SortedSet<Asset>
        get() = subscribedSymbols.map { assetMap.getValue(it) }.toSortedSet()

    /**
     * Connect to streaming data for the stock market and crypto market
     */
    fun connect() {
        connectMarket((alpacaAPI.stockMarketDataStreaming()))
        connectMarket((alpacaAPI.cryptoMarketDataStreaming()))
    }

    /**
     * Connect to ta market data provider and start listening. This can be the stocks or crypto market data feeds.
     */
    private fun connectMarket(connection: MarketDataWebsocketInterface) {
        require(!connection.isConnected) { "already connected, disconnect first" }
        val timeoutMillis: Long = 5_000

        connection.subscribeToControl(
            MarketDataMessageType.SUCCESS,
            MarketDataMessageType.SUBSCRIPTION,
            MarketDataMessageType.ERROR,
        )
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
            if (alpacaAPI.stockMarketDataStreaming().isConnected) alpacaAPI.stockMarketDataStreaming().disconnect()
            if (alpacaAPI.cryptoMarketDataStreaming().isConnected) alpacaAPI.cryptoMarketDataStreaming().disconnect()
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

    private fun validateSymbols(symbols: Array<out String>, map: Map<String, Asset>) {
        require(symbols.isNotEmpty()) { "provide at least one symbol" }
        symbols.forEach {
            require(map.contains(it) || it == "*") { "unknown symbol $it" }
        }
    }

    /**
     * Subscribe to stock market data based on the passed [symbols] and [type]
     */
    fun subscribeStocks(vararg symbols: String, type: PriceActionType = PriceActionType.PRICE_BAR) {
        validateSymbols(symbols, availableStocksMap)
        val s = symbols.toList()
        when (type) {
            PriceActionType.TRADE -> alpacaAPI.stockMarketDataStreaming().subscribe(s, null, null)
            PriceActionType.QUOTE -> alpacaAPI.stockMarketDataStreaming().subscribe(null, s, null)
            PriceActionType.PRICE_BAR -> alpacaAPI.stockMarketDataStreaming().subscribe(null, null, s)
        }
        subscribedSymbols.addAll(s)
    }

    /**
     * Subscribe to crypto market data b based on the passed [symbols] and [type]
     */
    fun subscribeCrypto(vararg symbols: String, type: PriceActionType = PriceActionType.PRICE_BAR) {
        validateSymbols(symbols, availableCryptoMap)
        val s = symbols.toList()
        when (type) {
            PriceActionType.TRADE -> alpacaAPI.cryptoMarketDataStreaming().subscribe(s, null, null)
            PriceActionType.QUOTE -> alpacaAPI.cryptoMarketDataStreaming().subscribe(null, s, null)
            PriceActionType.PRICE_BAR -> alpacaAPI.cryptoMarketDataStreaming().subscribe(null, null, s)
        }
        subscribedSymbols.addAll(s)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun handleMsg(msg: MarketDataMessage) {
        try {
            logger.trace { "received msg=$msg" }
            if (msg is SymbolMessage) {
                val asset = assetMap.getValue(msg.symbol)

                val action: PriceItem? = when (msg) {
                    is TradeMessage -> TradePrice(asset, msg.price)
                    is QuoteMessage -> PriceQuote(
                        asset,
                        msg.askPrice,
                        Double.NaN,
                        msg.bidPrice,
                        Double.NaN
                    )

                    is BarMessage -> PriceBar(
                        asset,
                        msg.open,
                        msg.high,
                        msg.low,
                        msg.close,
                        msg.tradeCount.toDouble()
                    )

                    else -> null
                }
                if (action != null) {
                    val now = Instant.now()
                    logger.trace { "received action=$action time=$now" }
                    val event = Event(listOf(action), now)
                    send(event)
                }
            } else if (msg is ErrorMessage) {
                logger.error(msg.message)
            }

        } catch (e: Throwable) {
            logger.warn(e) { "error during handling market data message" }
        }
    }

    private fun createListener(): MarketDataListener {
        return MarketDataListener { streamMessageType, msg ->
            logger.trace { "received message of type=$streamMessageType and msg=$msg" }

            when (streamMessageType) {
                MarketDataMessageType.ERROR -> logger.error("error msg=$msg")
                MarketDataMessageType.SUBSCRIPTION -> logger.info("subscription msg=$msg")
                MarketDataMessageType.SUCCESS -> logger.debug { "success msg=$msg" }
                else -> handleMsg(msg)
            }
        }
    }
}



