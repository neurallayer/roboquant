package org.roboquant.feeds.alpaca

import net.jacobpeterson.alpaca.AlpacaAPI
import net.jacobpeterson.alpaca.model.endpoint.asset.enums.AssetStatus
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.bar.BarMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.quote.QuoteMessage
import net.jacobpeterson.alpaca.model.endpoint.marketdata.realtime.trade.TradeMessage
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataListener
import org.roboquant.common.*
import org.roboquant.feeds.*
import java.time.Instant

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
class AlpacaFeed(apiKey: String? = null, apiSecret: String? = null, autoConnect: Boolean = true) : LiveFeed() {

    private val alpacaAPI: AlpacaAPI
    private val assetsMap = mutableMapOf<String, Asset>()

    val assets
        get() = assetsMap.values

    val logger = Logging.getLogger("AlpacaFeed")
    private val listener = createListener()

    init {
        val finalKey = apiKey ?: Config.getProperty("APCA-API-KEY-ID")
        val finalSecret = apiSecret ?: Config.getProperty("APCA-API-SECRET-KEY")
        require(finalKey != null) { "No public key provided or set as environment variable APCA-API-KEY-ID" }
        require(finalSecret != null) { "No secret key provided or set as environment variable APCA-API-SECRET-KEY" }


        alpacaAPI = AlpacaAPI(finalKey, finalSecret)

        if (autoConnect) connect()
        retrieveAssets()
        alpacaAPI.marketDataStreaming().setListener(listener)
    }

    /**
     * Get the available assets for trading and store them
     *
     * @return
     */
    private fun retrieveAssets() {
        val availableAssets = alpacaAPI.assets().get(AssetStatus.ACTIVE, "us_equity")
        assetsMap.clear()
        val exchangeCodes = Exchange.exchanges.map { e -> e.exchangeCode }
        availableAssets.forEach {
            if (it.exchange !in exchangeCodes) logger.warning("Exchange ${it.exchange} not known")
            assetsMap[it.symbol] = Asset(it.symbol, AssetType.STOCK, "USD", it.exchange, id = it.id)
        }
        logger.info("Found ${assetsMap.size} assets")
    }

    /**
     * Start listening for market data
     *
     */
    fun connect() {
        alpacaAPI.marketDataStreaming().connect()
    }

    /**
     * Stop listening for market data
     *
     */
    fun disconnect() {
        alpacaAPI.marketDataStreaming().disconnect()
    }


    fun subscribe(assets: Collection<Asset>) {
        subscribe(*assets.toTypedArray())
    }

    fun subscribe(vararg assets: Asset) {
        for (asset in assets) {
            require(asset.symbol in assetsMap) { "Asset $asset not active at Alpaca" }
            require(asset.type == AssetType.STOCK) { "Only stocks supported, received ${asset.type}" }
            require(asset.currencyCode == "USD") { "Only USD currency supported, received ${asset.currencyCode}" }
        }
        if (assets.isEmpty()) {
            alpacaAPI.marketDataStreaming().subscribe(null, null, listOf("*"))
            logger.info("Subscribed to all assets")
        } else {
            alpacaAPI.marketDataStreaming().subscribe(null, null, assets.map { it.symbol })
            logger.info("Subscribed to ${assets.size} assets")
        }
    }


    private fun createListener(): MarketDataListener {
        return MarketDataListener { streamMessageType, msg ->
            val action: PriceAction = when (msg) {
                is TradeMessage -> TradePrice(assetsMap[msg.symbol]!!, msg.price, msg.size.toDouble())
                is QuoteMessage -> PriceQuote(
                    assetsMap[msg.symbol]!!,
                    msg.askPrice,
                    msg.askSize.toDouble(),
                    msg.bidPrice,
                    msg.bidSize.toDouble()
                )
                is BarMessage -> PriceBar(
                    assetsMap[msg.symbol]!!,
                    msg.open.toFloat(),
                    msg.high.toFloat(),
                    msg.low.toFloat(),
                    msg.close.toFloat(),
                    msg.volume.toFloat()
                )
                else -> {
                    throw Exception("Unexpected type $streamMessageType")
                }
            }
            val now = Instant.now()
            val event = Event(listOf(action), now)
            channel?.offer(event)
        }
    }

}

