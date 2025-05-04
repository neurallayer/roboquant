/*
 * Copyright 2020-2025 Neural Layer
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
import net.jacobpeterson.alpaca.openapi.marketdata.api.StockApi
import net.jacobpeterson.alpaca.openapi.marketdata.model.Sort
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockAdjustment
import net.jacobpeterson.alpaca.openapi.marketdata.model.StockBar
import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import java.time.OffsetDateTime
import java.time.ZoneOffset


/**
 * Historic data feed using market data from Alpaca
 */
class AlpacaHistoricFeed(
    configure: AlpacaConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    // private val limit = 10_000L
    private val config = AlpacaConfig()
    private val stockData: StockApi
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaHistoricFeed::class)

    init {
        config.configure()
        alpacaAPI = Alpaca.getAPI(config)
        stockData = alpacaAPI.marketData().stock()
    }


    private fun toOffset(timeframe: Timeframe): Pair<OffsetDateTime, OffsetDateTime> {
        return Pair(
            timeframe.start.atOffset(ZoneOffset.UTC),
            timeframe.end.atOffset(ZoneOffset.UTC)
        )
    }

    /**
     * Retrieve the [PriceQuote] for a number of [symbols] and specified [timeframe].
     */
    fun retrieveStockQuotes(symbols: String, timeframe: Timeframe) {
        val (start, end) = toOffset(timeframe)
        var nextPageToken: String? = null
        do {
            val resp = stockData.stockQuotes(
                symbols, start, end, null, "", config.stockFeed, "USD", nextPageToken, Sort.ASC
            )
            for ((symbol, quotes) in resp.quotes) {
                val asset = Stock(symbol)
                for (quote in quotes) {
                    val action = PriceQuote(
                        asset,
                        quote.ap,
                        quote.`as`.toDouble(),
                        quote.bp,
                        quote.bs.toDouble()
                    )
                    val now = quote.t.toInstant()
                    add(now, action)
                }

            }
            nextPageToken = resp.nextPageToken
        } while (nextPageToken != null)
        logger.debug { "retrieved prices type=quotes symbol=$symbols timeframe=$timeframe" }
    }

    /**
     * Retrieve the [PriceQuote] for a number of [symbols] and specified [timeframe].
     */
    fun retrieveStockTrades(symbols: String, timeframe: Timeframe) {

        val (start, end) = toOffset(timeframe)
        var nextPageToken: String? = null
        do {
            val resp = stockData.stockTrades(
                symbols, start, end, null, "", config.stockFeed, "USD", nextPageToken, Sort.ASC
            )
            for ((symbol, trades) in resp.trades) {
                val asset = Stock(symbol)
                for (trade in trades) {
                    val action = TradePrice(asset, trade.p, trade.s.toDouble())
                    val now = trade.t.toInstant()
                    add(now, action)
                }
            }
            nextPageToken = resp.nextPageToken
        } while (nextPageToken != null)
        logger.debug { "retrieved prices type=trades symbols=$symbols timeframe=$timeframe" }

    }

    private fun processBars(symbol: String, bars: List<StockBar>, timeSpan: TimeSpan?) {
        val asset = Stock(symbol)
        for (bar in bars) {
            val action = PriceBar(asset, bar.o, bar.h, bar.l, bar.c, bar.v.toDouble(), timeSpan)
            val time = bar.t.toInstant()
            add(time, action)
        }
    }

    /**
     * Retrieve the [PriceBar]  for a number of [symbols] and the specified [timeframe].
     */
    fun retrieveStockPriceBars(
        symbols: String,
        timeframe: Timeframe,
        frequency: String = "1Day",
        adjustment: StockAdjustment = StockAdjustment.ALL
    ) {
        val (start, end) = toOffset(timeframe)

        var nextPageToken: String? = null
        do {
            val resp = stockData.stockBars(
                symbols,
                frequency,
                start,
                end,
                null,
                adjustment,
                null,
                config.stockFeed,
                "USD",
                nextPageToken,
                Sort.ASC
            )
            for ((symbol, bars) in resp.bars) {
                processBars(symbol, bars, null)
            }
            nextPageToken = resp.nextPageToken
        } while (nextPageToken != null)
        logger.debug { "retrieved prices type=bars symbol=$symbols timeframe=$timeframe" }
    }
}




