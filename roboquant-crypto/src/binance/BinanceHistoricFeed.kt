package org.roboquant.binance

import com.binance.api.client.BinanceApiRestClient
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.CryptoBuilder
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import java.time.Instant

/**
 * Create a new feed based on price actions coming from the Binance exchange.
 *
 * @property useMachineTime
 * @constructor
 *
 */
class BinanceHistoricFeed(apiKey: String? = null, secret:String? = null, private val useMachineTime: Boolean = true) : HistoricPriceFeed() {

    private var client: BinanceApiRestClient
    private val logger = Logging.getLogger(this)
    private val factory = BinanceConnection.getFactory(apiKey, secret)

    val availableAssets by lazy {
        BinanceConnection.retrieveAssets(factory)
    }

    init {
        client = factory.newRestClient()
    }


    fun retrieve(vararg currencyPairs:String, timeFrame: TimeFrame, interval: Interval = Interval.DAILY, limit: Int = 1000) {
        require(currencyPairs.isNotEmpty()) { "You need to provide at least 1 currency pair"}
        val startTime = timeFrame.start.toEpochMilli()
        val endTime = timeFrame.end.toEpochMilli() - 1 // Binance uses inclusive end-times, so we subtract 1 millis
        for (name in currencyPairs) {
            val asset = CryptoBuilder().invoke(name.uppercase(), binanceTemplate)
            val bars = client.getCandlestickBars(asset.symbol, interval, limit, startTime, endTime)
            for (bar in bars) {
                val action = PriceBar(asset, bar.open.toDouble(), bar.high.toDouble(), bar.low.toDouble(), bar.close.toDouble(), bar.volume.toDouble())
                val now = Instant.ofEpochMilli(bar.closeTime)
                val list = events.getOrPut(now) { mutableListOf() }
                list.add(action)
            }
            logger.fine { "Retrieved $asset for $timeFrame"}
        }
    }


}

