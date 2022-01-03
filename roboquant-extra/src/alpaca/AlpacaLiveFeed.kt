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

package org.roboquant.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.MarketDataMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.bar.BarMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.enums.MarketDataMessageType
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.quote.QuoteMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.realtime.trade.TradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.feeds.*
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Alpaca feed allows you to subscribe to live market data from Alpaca. Alpaca needs a key and secret in order to access
 * their API.
 *
 * You can provide these to the constructor or set them as environment variables ("APCA_API_KEY_ID", "APCA_API_SECRET_KEY").
 *
 * @constructor
 *
 * @param apiKey
 * @param apiSecret
 */
class AlpacaLiveFeed(
    apiKey: String? = null,
    apiSecret: String? = null,
    accountType: AccountType = AccountType.PAPER,
    dataType: DataType = DataType.IEX,
    autoConnect: Boolean = true
) : LiveFeed() {

    private val alpacaAPI: AlpacaAPI = AlpacaConnection.getAPI(apiKey, apiSecret, accountType, dataType)


    val logger = Logging.getLogger(this)
    private val listener = createListener()

    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }

    private val assetsMap by lazy {
        availableAssets.associateBy { it.symbol }
    }

    val assets
        get() = assetsMap.values.toSortedSet()


    init {
        if (autoConnect) connect()
        alpacaAPI.stockMarketDataStreaming().setListener(listener)
    }


    /**
     * Start listening for market data
     */
    fun connect() {
        require(!alpacaAPI.stockMarketDataStreaming().isConnected) { "Already connected, disconnect first" }

        alpacaAPI.stockMarketDataStreaming().subscribeToControl(
            MarketDataMessageType.SUCCESS,
            MarketDataMessageType.SUBSCRIPTION,
            MarketDataMessageType.ERROR
        )
        alpacaAPI.stockMarketDataStreaming().connect()
        alpacaAPI.stockMarketDataStreaming().waitForAuthorization(5, TimeUnit.SECONDS)
        if (!alpacaAPI.stockMarketDataStreaming().isValid) {
            logger.severe("Couldn't establish websocket connection")
        }
    }

    /**
     * Stop listening for market data
     */
    fun disconnect() {
        try {
            if (alpacaAPI.stockMarketDataStreaming().isConnected) alpacaAPI.stockMarketDataStreaming().disconnect()
        } catch (_: Exception) {
        }
    }

    /**
     * Just cleanup a any used connection
     */
    fun finalize() {
        disconnect()
    }

    fun subscribe(assets: Collection<Asset>) {
        subscribe(*assets.toTypedArray())
    }

    /**
     * Subscribe to price data of all the assets
     */
    fun subscribeAll() {
        alpacaAPI.stockMarketDataStreaming().subscribe(null, null, listOf("*"))
        logger.info("Subscribing to all assets")
    }

    /**
     * Subscribe to price data identified by their [assets]
     */
    fun subscribe(vararg assets: Asset) {
        for (asset in assets) {
            require(asset.type == AssetType.STOCK) { "Only stocks are supported, received ${asset.type}" }
            require(asset.currencyCode == "USD") { "Only USD currency supported, received ${asset.currencyCode}" }
        }

        val symbols = assets.map { it.symbol }
        alpacaAPI.stockMarketDataStreaming().subscribe(null, null, symbols)
        logger.info("Subscribing to ${assets.size} assets")
    }

    /**
     * Subscribe to price data identified by their [symbols]
     */
    fun subscribe(vararg symbols: String) {
        val assets = symbols.map { Asset(it) }.toTypedArray()
        subscribe(*assets)
    }

    private fun handleMsg(msg: MarketDataMessage) {
        try {
            logger.finer {"Received msg $msg"}
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
                else -> {
                    logger.warning("Unexpected msg $msg")
                    null
                }
            }

            if (action != null) {
                val now = Instant.now()
                val event = Event(listOf(action), now)
                channel?.offer(event)
            }
        } catch (e: Exception) {
            logger.warning(e.message)
        }
    }


    private fun createListener(): MarketDataListener {
        return MarketDataListener { streamMessageType, msg ->
            logger.finer {"Received message of type $streamMessageType and msg $msg"}

            when (streamMessageType) {
                MarketDataMessageType.ERROR -> logger.warning(msg.toString())
                MarketDataMessageType.SUBSCRIPTION -> logger.info(msg.toString())
                else -> handleMsg(msg)
            }
        }
    }
}



