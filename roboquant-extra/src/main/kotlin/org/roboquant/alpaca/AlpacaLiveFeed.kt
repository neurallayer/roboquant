/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.MarketDataMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.bar.BarMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.control.SuccessMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.enums.MarketDataMessageType
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.quote.QuoteMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.trade.TradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataWebsocketInterface
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.common.severe
import org.roboquant.feeds.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Alpaca feed allows you to subscribe to live market data from Alpaca. Alpaca needs a key and secret in order to access
 * their API. This live feed support both stocks and crypto asset classes.
 *
 * You can provide these to the constructor or set them as environment variables.
 *
 * @constructor
 *
 */
class AlpacaLiveFeed(
    autoConnect: Boolean = true,
    configure: AlpacaConfig.() -> Unit = {}
) : LiveFeed(), AssetFeed {

    val config = AlpacaConfig()
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaLiveFeed::class)
    private val listener = createListener()

    private val assetsMap by lazy {
        availableAssets.associateBy { it.symbol }
    }

    private fun String.isCrypto() = assetsMap[this]!!.type == AssetType.CRYPTO

    /**
     * TODO return only subscribed assets
     */
    override val assets
        get() = assetsMap.values.toSortedSet()

    init {
        config.configure()
        alpacaAPI = AlpacaConnection.getAPI(config)
        if (autoConnect) {
            connect(alpacaAPI.stockMarketDataStreaming())
            connect(alpacaAPI.cryptoMarketDataStreaming())
        }
    }

    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI).values.toSortedSet()
    }

    /**
     * Connect to ta market data provider and start listening. This can be the stocks or crypto market data feeds.
     */
    fun connect(
        connection: MarketDataWebsocketInterface = alpacaAPI.stockMarketDataStreaming(),
        timeoutMillis: Long = 5_000
    ) {
        require(!connection.isConnected) { "Already connected, disconnect first" }

        connection.subscribeToControl(
            MarketDataMessageType.SUCCESS,
            MarketDataMessageType.SUBSCRIPTION,
            MarketDataMessageType.ERROR
        )
        connection.connect()
        connection.waitForAuthorization(timeoutMillis, TimeUnit.MILLISECONDS)
        if (!connection.isValid) {
            logger.severe("Couldn't establish websocket connection")
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
            alpacaAPI.okHttpClient.dispatcher.executorService.shutdown()
            alpacaAPI.okHttpClient.connectionPool.evictAll()
        } catch (exception: Exception) {
            logger.info(exception.message)
        }
    }

    /**
     * Just cleanup any used connection
     */
    fun finalize() {
        close()
    }

    private val Collection<Asset>.symbols
        get() = if (isEmpty()) null else map { it.symbol }.distinct()

    fun subscribeStocks(
        priceBars: Collection<String>,
        trades: Collection<String> = emptyList(),
        quotes: Collection<String> = emptyList()
    ) {
        alpacaAPI.stockMarketDataStreaming()
            .subscribe(trades.ifEmpty { null }, quotes.ifEmpty { null }, priceBars.ifEmpty { null })
    }

    @JvmName("subscribeStocksAssets")
    fun subscribeStocks(
        priceBars: Collection<Asset>,
        trades: Collection<Asset> = emptyList(),
        quotes: Collection<Asset> = emptyList()
    ) {
        alpacaAPI.stockMarketDataStreaming().subscribe(trades.symbols, quotes.symbols, priceBars.symbols)
    }

    fun subscribeCrypto(
        priceBars: Collection<String>,
        trades: Collection<String> = emptyList(),
        quotes: Collection<String> = emptyList()
    ) {
        alpacaAPI.cryptoMarketDataStreaming()
            .subscribe(trades.ifEmpty { null }, quotes.ifEmpty { null }, priceBars.ifEmpty { null })
    }

    @JvmName("subscribeCryptoAssets")
    fun subscribeCrypto(
        priceBars: Collection<Asset>,
        trades: Collection<Asset> = emptyList(),
        quotes: Collection<Asset> = emptyList()
    ) {
        alpacaAPI.cryptoMarketDataStreaming().subscribe(trades.symbols, quotes.symbols, priceBars.symbols)
    }

    /**
     * Subscribe to price bar data identified by their [symbols].
     */
    fun subscribe(vararg symbols: String) {
        val crypto = symbols.filter { it.isCrypto() }
        if (crypto.isNotEmpty()) subscribeCrypto(crypto)

        val stocks = symbols.filter { !it.isCrypto() }
        if (stocks.isNotEmpty()) subscribeStocks(stocks)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun handleMsg(msg: MarketDataMessage) {
        try {
            logger.finer { "Received msg $msg" }
            val action: PriceAction? = when (msg) {
                is TradeMessage -> TradePrice(assetsMap[msg.symbol]!!, msg.price)
                is QuoteMessage -> PriceQuote(
                    assetsMap[msg.symbol]!!,
                    msg.askPrice,
                    Double.NaN,
                    msg.bidPrice,
                    Double.NaN
                )

                is BarMessage -> PriceBar(
                    assetsMap[msg.symbol]!!,
                    msg.open,
                    msg.high,
                    msg.low,
                    msg.close,
                    msg.tradeCount.toDouble()
                )

                is SuccessMessage -> {
                    logger.fine(msg.message); null
                }

                else -> {
                    logger.warning("Unexpected msg $msg"); null
                }
            }

            if (action != null) {
                val now = Instant.now()
                logger.finer { "Received action $action at ${now.truncatedTo(ChronoUnit.SECONDS)}" }
                val event = Event(listOf(action), now)
                send(event)
            }
        } catch (e: Throwable) {
            logger.severe("error during handling market data message", e)
        }
    }

    private fun createListener(): MarketDataListener {
        return MarketDataListener { streamMessageType, msg ->
            logger.finer { "Received message of type=$streamMessageType and msg=$msg" }

            when (streamMessageType) {
                MarketDataMessageType.ERROR -> logger.warning("$msg")
                MarketDataMessageType.SUBSCRIPTION -> logger.info("subscription $msg")
                MarketDataMessageType.SUCCESS -> logger.fine("success $msg")
                else -> handleMsg(msg)
            }
        }
    }
}



