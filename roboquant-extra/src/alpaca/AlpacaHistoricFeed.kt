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
import net.jacobpeterson.alpaca.model.properties.DataAPIType
import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAmount

typealias AlpacaPeriod = BarTimePeriod

/**
 * Historic data feed using market data from Alpaca
 */
class AlpacaHistoricFeed(
    configure: AlpacaConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    val config = AlpacaConfig()
    private val alpacaAPI: AlpacaAPI
    private val logger = Logging.getLogger(AlpacaHistoricFeed::class)
    private val zoneId: ZoneId = ZoneId.of("America/New_York")

    init {
        config.configure()
        alpacaAPI = AlpacaConnection.getAPI(config)
    }

    /**
     * All available assets that can be retrieved. See [assets] for the assets that have already been retreived.
     */
    val availableAssets by lazy {
        AlpacaConnection.getAvailableAssets(alpacaAPI)
    }

    /**
     * Retrieve for a number of [symbols] and specified [timeframe] the [PriceQuote].
     */
    fun retrieveQuotes(vararg symbols: String, timeframe: Timeframe) {
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getQuotes(
                symbol,
                ZonedDateTime.ofInstant(timeframe.start, zoneId),
                ZonedDateTime.ofInstant(timeframe.end, zoneId),
                null,
                null,
            )
            resp.quotes == null && continue
            val asset = Asset(symbol)
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
            logger.fine { "Retrieved quote prices for asset $asset and $timeframe" }
        }
    }

    /**
     * Retrieve for a number of [symbols] and specified [timeframe] the [TradePrice].
     */
    fun retrieveTrades(vararg symbols: String, timeframe: Timeframe) {
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getTrades(
                symbol,
                ZonedDateTime.ofInstant(timeframe.start, zoneId),
                ZonedDateTime.ofInstant(timeframe.end, zoneId),
                null,
                null,
            )

            resp.trades == null && continue
            val asset = Asset(symbol)
            for (trade in resp.trades) {
                val action = TradePrice(asset, trade.price, trade.size.toDouble())
                val now = trade.timestamp.toInstant()
                add(now, action)
            }
            logger.fine { "Retrieved trade prices for asset $asset and $timeframe" }
        }
    }

    private fun fromTemporalAmount(amt: TemporalAmount): Pair<BarTimePeriod, Int> {

        return when {
            amt is ZonedPeriod && amt.units.contains(ChronoUnit.DAYS) && amt.toDays()
                .toInt() > 0 -> Pair(AlpacaPeriod.DAY, amt.toDays().toInt())
            amt is ZonedPeriod && amt.toHours() > 0 -> Pair(AlpacaPeriod.HOUR, amt.toHours().toInt())
            amt is ZonedPeriod && amt.toMinutes() > 0 -> Pair(AlpacaPeriod.MINUTE, amt.toMinutes().toInt())
            else -> throw UnsupportedException("$amt")
        }

    }

    /**
     * Retrieve for a number of [symbols] and specified [timeframe] the [PriceBar] and [barSize].
     */
    fun retrieveBars(
        vararg symbols: String,
        timeframe: Timeframe,
        barSize: TemporalAmount = 1.days,
    ) {
        val barFeed = when (config.dataType) {
            DataAPIType.IEX -> BarFeed.IEX
            DataAPIType.SIP -> BarFeed.SIP
        }
        val (alpacaPeriod, duration) = fromTemporalAmount(barSize)
        for (symbol in symbols) {
            val resp = alpacaAPI.stockMarketData().getBars(
                symbol,
                ZonedDateTime.ofInstant(timeframe.start, zoneId),
                ZonedDateTime.ofInstant(timeframe.end, zoneId),
                null,
                null,
                duration,
                alpacaPeriod,
                BarAdjustment.ALL,
                barFeed
            )
            resp.bars == null && continue

            val asset = Asset(symbol)
            for (bar in resp.bars) {
                val action = PriceBar(asset, bar.open, bar.high, bar.low, bar.close, bar.volume.toDouble())
                val now = bar.timestamp.toInstant()
                add(now, action)
            }
            logger.fine { "Retrieved price bars for asset $asset and timeframe $timeframe" }
        }
    }

}

