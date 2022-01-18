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
import net.jacobpeterson.alpaca.model.endpoint.marketdata.common.historical.bar.enums.BarTimePeriod
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarAdjustment
import net.jacobpeterson.alpaca.model.endpoint.marketdata.stock.historical.bar.enums.BarFeed
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import java.time.ZoneId
import java.time.ZonedDateTime

typealias AlpacaPeriod = BarTimePeriod

/**
 * Get historic data feed from Alpaca
 */
class AlpacaHistoricFeed(
    apiKey: String? = null,
    apiSecret: String? = null,
    accountType: AccountType = AccountType.PAPER,
    dataType: DataType = DataType.IEX
) : HistoricPriceFeed() {

    private val alpacaAPI: AlpacaAPI = AlpacaConnection.getAPI(apiKey, apiSecret, accountType, dataType)
    private val logger = Logging.getLogger(AlpacaHistoricFeed::class)
    private val zoneId: ZoneId = ZoneId.of("America/New_York")


    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }


    fun retrieveQuotes(vararg symbols: String, timeFrame: TimeFrame) {
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getQuotes(
                symbol,
                ZonedDateTime.ofInstant(timeFrame.start, zoneId),
                ZonedDateTime.ofInstant(timeFrame.end, zoneId),
                null,
                null,
            )

            val asset = Asset(symbol)
            for (quote in resp.quotes) {
                val action = PriceQuote(asset, quote.askPrice, quote.askSize.toDouble(), quote.bidPrice, quote.bidSize.toDouble())
                val now = quote.timestamp.toInstant()
                add(now, action)
            }
            logger.fine { "Retrieved quote prices for asset $asset and $timeFrame" }
        }
    }

    fun retrieveTrades(vararg symbols: String, timeFrame: TimeFrame) {
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getTrades(
                symbol,
                ZonedDateTime.ofInstant(timeFrame.start, zoneId),
                ZonedDateTime.ofInstant(timeFrame.end, zoneId),
                null,
                null,
            )

            val asset = Asset(symbol)
            for (trade in resp.trades) {
                val action = TradePrice(asset, trade.price, trade.size.toDouble())
                val now = trade.timestamp.toInstant()
                add(now, action)
            }
            logger.fine { "Retrieved trade prices for asset $asset and $timeFrame" }
        }
    }


    fun retrieveBars(vararg symbols: String, timeFrame: TimeFrame, period: AlpacaPeriod = AlpacaPeriod.DAY) {
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getBars(
                symbol,
                ZonedDateTime.ofInstant(timeFrame.start, zoneId),
                ZonedDateTime.ofInstant(timeFrame.end, zoneId),
                null,
                null,
                1,
                period,
                BarAdjustment.ALL,
                BarFeed.IEX
            )

            val asset = Asset(symbol)
            for (bar in resp.bars) {
                val action = PriceBar(asset, bar.open, bar.high, bar.low, bar.close, bar.volume.toDouble())
                val now = bar.timestamp.toInstant()
                add(now, action)
            }
            logger.fine { "Retrieved price bars for asset $asset and time frame $timeFrame" }
        }
    }


}

