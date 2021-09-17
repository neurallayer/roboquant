package org.roboquant.feeds.iex

import org.roboquant.common.Asset
import org.roboquant.common.Config
import org.roboquant.common.Logging
import org.roboquant.feeds.Action
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.TradePrice
import pl.zankowski.iextrading4j.api.marketdata.Trade
import pl.zankowski.iextrading4j.api.stocks.Quote
import pl.zankowski.iextrading4j.client.IEXCloudClient
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion
import pl.zankowski.iextrading4j.client.IEXTradingClient
import pl.zankowski.iextrading4j.client.socket.request.marketdata.deep.DeepAsyncResponse
import pl.zankowski.iextrading4j.client.sse.request.marketdata.TradesSseRequestBuilder
import pl.zankowski.iextrading4j.client.sse.request.stocks.QuoteInterval
import pl.zankowski.iextrading4j.client.sse.request.stocks.QuoteSseRequestBuilder
import java.time.Instant

/**
 * Live feed of trades on IEX for the subscribed assets. For each trade the sale price and volume is provided
 * together when the trade happened.
 *
 * @constructor
 *
 * @param publicKey
 * @param secretKey
 */
class IEXLive(publicKey: String? = null, secretKey: String? = null, private val useMachineTime: Boolean = true) :
    LiveFeed() {

    private val logger = Logging.getLogger("IEXLive")
    private val cloudClient: IEXCloudClient
    private val assetMap = mutableMapOf<String, Asset>()

    val assets
        get() = assetMap.values.distinct()

    init {
        val pToken = publicKey ?: Config.getProperty("IEX_PUBLIC_KEY")
        require(pToken != null)
        val sToken = secretKey ?: Config.getProperty("IEX_SECRET_KEY")

        var tokenBuilder = IEXCloudTokenBuilder().withPublishableToken(pToken)
        if (sToken != null)
            tokenBuilder = tokenBuilder.withSecretToken(sToken)

        cloudClient = IEXTradingClient.create(IEXTradingApiVersion.IEX_CLOUD_V1, tokenBuilder.build())
    }

    /**
     * Subscribe to one or more assets. If the symbol of the asset is found by IEX, [TradePrice] will be provided
     * as part of the feed.
     *
     * @param assets
     */
    fun subscribeQuotes(vararg assets: Asset, interval: QuoteInterval = QuoteInterval.ONE_MINUTE) {

        logger.info { "Subscribing to assets $assets" }
        val symbols = assets.map { it.symbol }.toTypedArray()
        assets.forEach { assetMap[it.symbol] = it }

        val request = QuoteSseRequestBuilder() // TopsSseRequestBuilder()
            .withSymbols(*symbols)
            .withQuoteInterval(interval)
            .build()

        cloudClient.subscribe(request, ::handleQuotes)
    }

    fun subscribeTrades(vararg assets: Asset) {
        val symbols = assets.map { it.symbol }.toTypedArray()
        assets.forEach { assetMap[it.symbol] = it }

        logger.info { "Subscribing to asset $assets" }

        val request = TradesSseRequestBuilder()
            .withSymbols(*symbols)
            .build()

        cloudClient.subscribe(request, ::handleTrades)
    }


    private fun handleTrades(trades: List<DeepAsyncResponse<Trade>>) {
        var now = Instant.now()
        var lastTime = Instant.MIN
        var actions = mutableListOf<Action>()
        for (trade in trades) {
            val asset  = assetMap[trade.symbol]!!
            val data = trade.data
            if (!useMachineTime) now = Instant.ofEpochMilli(trade.data.timestamp)
            val action = TradePrice(asset, data.price.toDouble(), data.size.toDouble())
            if (now > lastTime && actions.isNotEmpty()) {
                val event = Event(actions, now)
                actions = mutableListOf()
                channel?.offer(event)
            }
            lastTime = now
            actions.add(action)
        }

        if (actions.isNotEmpty()) {
            val event = Event(actions, now)
            channel?.offer(event)
        }
    }


    // TODO correct for timezone
    private fun handleQuotes(quotes: List<Quote>) {
        logger.info { "Received callback" }
        println(quotes.size)
        var now = Instant.now()
        quotes.forEach {
            val asset  = assetMap[it.symbol]!!
            val action = TradePrice(asset, it.iexRealtimePrice.toDouble(), it.iexRealtimeSize.toDouble())
            if (!useMachineTime) now = Instant.ofEpochMilli(it.iexLastUpdated)
            val event = Event(listOf(action), now)
            channel?.offer(event)
        }

    }

}
