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
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.StockBarsResponse
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarAdjustment
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarFeed
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import net.jacobpeterson.alpaca.rest.endpoint.marketdata.stock.StockMarketDataEndpoint
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * @see BarTimePeriod
 */
typealias BarPeriod = BarTimePeriod


/**
 * Historic data feed using market data from Alpaca
 */
class AlpacaHistoricFeed(
    configure: AlpacaConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val limit = 10_000
    private val config = AlpacaConfig()
    private val stockData: StockMarketDataEndpoint
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaHistoricFeed::class)
    private val zoneId: ZoneId = ZoneId.of("America/New_York")

    init {
        config.configure()
        alpacaAPI = Alpaca.getAPI(config)
        stockData = alpacaAPI.stockMarketData()
    }

    private val availableStocks: Map<String, Asset> by lazy {
        Alpaca.getAvailableStocks(alpacaAPI)
    }

    private val availableCrypto: Map<String, Asset> by lazy {
        Alpaca.getAvailableCrypto(alpacaAPI)
    }

    /**
     * Get the available assets to retrieve
     */
    val availableAssets: SortedSet<Asset>
        get() = (availableStocks.values + availableCrypto.values).toSortedSet()

    private fun getAsset(symbol: String) = availableStocks[symbol] ?: availableCrypto[symbol]

    private val Instant.zonedDateTime
        get() = ZonedDateTime.ofInstant(this, zoneId)


    private fun validateStockSymbols(symbols: Array<out String>) {
        require(symbols.isNotEmpty()) { "specify at least one symbol" }
        symbols.forEach {
            require(availableStocks[it] != null){ "unknown symbol $it"}
        }
    }

    /**
     * Retrieve the [PriceQuote] for a number of [symbols] and specified [timeframe]. If there is more than one quote
     * for a certain asset at a certain point in time, the latest one will be stored.
     */
    fun retrieveStockQuotes(vararg symbols: String, timeframe: Timeframe) {
        validateStockSymbols(symbols)

        val start = timeframe.start.zonedDateTime
        val end = timeframe.end.zonedDateTime
        for (symbol in symbols) {
            var nextPageToken: String? = null
            do {
                val resp = stockData.getQuotes(symbol, start, end, limit, nextPageToken)
                resp.quotes == null && return
                val asset = getAsset(symbol)!!
                for (quote in resp.quotes) {
                    val action = PriceQuote(
                        asset,
                        quote.askPrice,
                        quote.askSize.toDouble(),
                        quote.bidPrice,
                        quote.bidSize.toDouble()
                    )
                    val now = quote.timestamp.toInstant()
                    add(now, action)
                }
                nextPageToken = resp.nextPageToken
            } while (nextPageToken != null)
            logger.debug { "retrieved prices type=quotes symbol=$symbol timeframe=$timeframe" }
        }

    }


    /**
     * Retrieve the [PriceQuote] for a number of [symbols] and specified [timeframe].
     */
    fun retrieveStockTrades(vararg symbols: String, timeframe: Timeframe) {
        validateStockSymbols(symbols)

        val start = timeframe.start.zonedDateTime
        val end = timeframe.end.zonedDateTime
        for (symbol in symbols) {
            var nextPageToken: String? = null
            do {
                val resp = stockData.getTrades(symbol, start, end, limit, nextPageToken)
                resp.trades == null && return
                val asset = getAsset(symbol)!!

                for (trade in resp.trades) {
                    val action = TradePrice(asset, trade.price, trade.size.toDouble())
                    val now = trade.timestamp.toInstant()
                    add(now, action)
                }
                nextPageToken = resp.nextPageToken
            } while (nextPageToken != null)
            logger.debug { "retrieved prices type=trades symbol=$symbol timeframe=$timeframe" }
        }

    }

    private fun processBars(symbol: String, resp: StockBarsResponse) {
        resp.bars == null && return
        val asset = getAsset(symbol)!!
        for (bar in resp.bars) {
            val action = PriceBar(asset, bar.open, bar.high, bar.low, bar.close, bar.volume.toDouble())
            val now = bar.timestamp.toInstant()
            add(now, action)
        }
    }

    /**
     * Retrieve the [PriceBar]  for a number of [symbols] and the specified [timeframe], [barDuration] and [barPeriod].
     */
    fun retrieveStockPriceBars(
        vararg symbols: String,
        timeframe: Timeframe,
        barDuration: Int = 1,
        barPeriod: BarPeriod = BarPeriod.DAY
    ) {
        validateStockSymbols(symbols)

        val barFeed = when (config.dataType) {
            DataAPIType.IEX -> BarFeed.IEX
            DataAPIType.SIP -> BarFeed.SIP
        }
        val start = timeframe.start.zonedDateTime
        val end = timeframe.end.zonedDateTime

        for (symbol in symbols) {
            var nextPageToken: String? = null
            do {
                val resp = stockData.getBars(
                    symbol,
                    start,
                    end,
                    limit,
                    nextPageToken,
                    barDuration,
                    barPeriod,
                    BarAdjustment.ALL,
                    barFeed
                )
                processBars(symbol, resp)
                nextPageToken = resp.nextPageToken
            } while (nextPageToken != null)
            logger.debug { "retrieved prices type=bars symbol=$symbol timeframe=$timeframe" }
        }
    }

}


