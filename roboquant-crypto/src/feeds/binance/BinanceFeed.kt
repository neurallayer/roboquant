package org.roboquant.feeds.binance

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.CandlestickInterval
import org.roboquant.common.Asset
import org.roboquant.common.AssetType
import org.roboquant.common.Logging
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.PriceBar
import java.io.Closeable
import java.time.Instant

/**
 * Create a new feed based on price actions coming from the Binance exchange.
 *
 * @property useMachineTime
 * @constructor
 *
 * @param client
 */
class BinanceFeed(client: BinanceApiWebSocketClient? = null, private val useMachineTime: Boolean = true) : LiveFeed() {

    private val client = client ?: BinanceApiClientFactory.newInstance().newWebSocketClient()
    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger("BinanceFeed")
    private val closeables = mutableListOf<Closeable>()
    private val intervals = CandlestickInterval.values().associateBy { it.intervalId }

    /**
     * What are the possible values for price-bar interval identifiers
     */
    val intervalIds
        get() = intervals.keys

    /**
     * Get the assets that has been subscribed to
     */
    val assets
        get() = subscriptions.values.distinct()

    init {
        logger.info { "Started BinanceFeed using web-socket client" }
    }


    /**
     * Subscribe to the [PriceBar] actions one or more currency pairs.
     *
     * @param currencyPairs the currency pairs you want to subscribe to
     * @param intervalId the interval of the PriceBar. Default is "1m" or 1 minute
     */
    fun subscribePriceBar(
        vararg currencyPairs: Pair<String, String>,
        intervalId: String = "1m"
    ) {
        require(currencyPairs.isNotEmpty())
        require(intervalId in intervalIds)

        val candlestickInterval = intervals[intervalId]
        for (currencyPair in currencyPairs) {
            val (symbol, currencyCode) = currencyPair
            val upperCaseSymbol = symbol.uppercase()
            logger.info { "Subscribing to $upperCaseSymbol" }
            val asset = getAsset(upperCaseSymbol, currencyCode)

            // API required lowercase symbol
            val closable = client.onCandlestickEvent(symbol.lowercase(), candlestickInterval) {
                handle(it)
            }
            closeables.add(closable)
            subscriptions[upperCaseSymbol] = asset
        }
    }

    fun disconnect() {
        for (c in closeables) c.close()
        closeables.clear()
    }

    private fun handle(resp: CandlestickEvent) {
        if (! resp.barFinal) return

        logger.finer { "Received candlestick event for symbol ${resp.symbol}" }
        val asset = subscriptions[resp.symbol]
        if (asset != null) {
            val action = PriceBar(
                asset,
                resp.open.toFloat(),
                resp.high.toFloat(),
                resp.low.toFloat(),
                resp.close.toFloat(),
                resp.volume.toFloat()
            )
            val now = if (useMachineTime) Instant.now() else Instant.ofEpochMilli(resp.closeTime)
            val event = Event(listOf(action), now)
            channel?.offer(event)
        } else {
            logger.warning { "Received CandlestickEvent for unexpected symbol ${resp.symbol}" }
        }
    }


    /**
     * Create an asset based on a currency pair.
     *
     * @param symbol
     * @return
     */
    private fun getAsset(symbol: String, currencyCode: String): Asset {
        return Asset(
            symbol = symbol,
            currencyCode = currencyCode,
            exchangeCode = "Binance",
            type = AssetType.CRYPTO
        )
    }


}

