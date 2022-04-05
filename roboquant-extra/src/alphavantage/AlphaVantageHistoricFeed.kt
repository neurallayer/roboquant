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

package org.roboquant.alphavantage

import com.crazzyghost.alphavantage.AlphaVantage
import com.crazzyghost.alphavantage.Config
import com.crazzyghost.alphavantage.parameters.OutputSize
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse
import org.roboquant.alpaca.AlpacaHistoricFeed
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.crazzyghost.alphavantage.parameters.Interval as AlphaInterval

typealias Interval = AlphaInterval


data class AlphaVantageConfig(
    var key: String = org.roboquant.common.Config.getProperty("alphavantage.key", ""),
)

/**
 * AlphaVantage feed, currently just a PoC to validate we can retrieve data.
 *
 * @property compensateTimeZone compensate for timezone differences
 * @property generateSinglePrice generate a single price event (using the close price) or a price bar (OHLCV) event
 * @constructor
 *
 */
class AlphaVantageHistoricFeed(
    val compensateTimeZone: Boolean = true,
    private val generateSinglePrice: Boolean = false,
    configure: AlphaVantageConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val subscriptions = mutableMapOf<String, Asset>()
    private val logger = Logging.getLogger(AlpacaHistoricFeed::class)
    val config = AlphaVantageConfig()

    init {
        config.configure()
        require(config.key.isNotBlank()) { "No api key provided" }

        val cfg = Config.builder()
            .key(config.key)
            .timeOut(10)
            .build()
        AlphaVantage.api().init(cfg)
        logger.info("Connected Alpha Vantage")
    }


    /**
     * Retrieve historic intra-day price data for the provided [assets]
     *
     */
    fun retrieveIntraday(vararg assets: Asset, interval: Interval = Interval.FIVE_MIN) {
        for (asset in assets) {
            val symbol = asset.symbol
            subscriptions[symbol] = asset
            val result = AlphaVantage.api()
                .timeSeries()
                .intraday()
                .forSymbol(symbol)
                .interval(interval)
                .outputSize(OutputSize.FULL)
                .fetchSync()

            if (result.errorMessage != null)
                logger.warning(result.errorMessage)
            else
                handleIntraday(result)
        }
    }


    /**
     * Retrieve historic daily price data for the provided [assets]
     *
     */
    fun retrieveDaily(vararg assets: Asset) {

        for (asset in assets) {
            val symbol = asset.symbol
            subscriptions[symbol] = asset
            val result = AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(symbol)
                .outputSize(OutputSize.FULL)
                .fetchSync()

            if (result.errorMessage != null)
                logger.warning(result.errorMessage)
            else
                handleDaily(result)
        }
    }


    private fun getParser(timezone: String): DateTimeFormatter {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val zoneId = if (compensateTimeZone) ZoneId.of(timezone) else ZoneId.of("UTC")
        return DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
    }

    private fun handleIntraday(response: TimeSeriesResponse) {
        try {
            val symbol = response.metaData.symbol
            logger.info { "Received time series response for $symbol" }
            val asset = subscriptions[symbol]!!
            val tz = response.metaData.timeZone ?: "America/New_York"
            val dtf = getParser(tz)
            response.stockUnits.forEach {
                val action = if (generateSinglePrice) TradePrice(asset, it.close) else
                    PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)

                val now = ZonedDateTime.parse(it.date, dtf).toInstant()
                add(now, action)
            }
            logger.info { "Received prices for $symbol" }
        } catch (e: Exception) {
            logger.severe { e.toString() }
        }
    }


    private fun handleDaily(response: TimeSeriesResponse) {
        try {
            val symbol = response.metaData.symbol
            logger.info { "Received time series response for $symbol" }
            val asset = subscriptions[symbol]!!
            response.stockUnits.forEach {
                val action = if (generateSinglePrice) TradePrice(asset, it.close) else
                    PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)

                val localDate = LocalDate.parse(it.date)
                val now = asset.exchange.getClosingTime(localDate)
                add(now, action)
            }
            logger.info { "Received prices for $symbol" }
        } catch (e: Exception) {
            logger.severe { e.toString() }
        }
    }


}