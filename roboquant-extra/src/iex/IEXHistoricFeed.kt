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
package org.roboquant.iex


import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import pl.zankowski.iextrading4j.api.stocks.Chart
import pl.zankowski.iextrading4j.api.stocks.ChartRange
import pl.zankowski.iextrading4j.api.stocks.v1.Intraday
import pl.zankowski.iextrading4j.client.IEXCloudClient
import pl.zankowski.iextrading4j.client.rest.request.stocks.ChartRequestBuilder
import pl.zankowski.iextrading4j.client.rest.request.stocks.v1.IntradayRequestBuilder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

typealias Range = ChartRange

/**
 * Feed of historic price data using IEX Cloud as the data source.
 *
 * @constructor
 */
class IEXHistoricFeed(
    private val template: Asset = Asset("TEMPLATE"),
    configure: IEXConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    val config = IEXConfig()
    private val logger = Logging.getLogger(IEXHistoricFeed::class)
    private val client: IEXCloudClient


    init {
        config.configure()
        client = IEXConnection.getClient(config)
    }


    /**
     * Retrieve historic intraday price bars for one or more assets
     *
     * @param assets
     */
    fun retrieveIntraday(assets: Collection<Asset>) {
        assets.forEach {
            val quote = client.executeRequest(
                IntradayRequestBuilder()
                    .withSymbol(it.symbol)
                    .build()
            )
            handleIntraday(it, quote)
        }
    }

    /**
     * Retrieve historic end of day [PriceBar] for one or more symbols
     *
     * @param symbols
     */
    fun retrieveIntraday(vararg symbols: String) {
        val assets = symbols.map { template.copy(symbol = it.uppercase()) }
        retrieveIntraday(assets.toList())
    }

    /**
     * Retrieve historic end of day [PriceBar] for one or more symbols
     *
     * @param symbols
     */
    fun retrieve(vararg symbols: String, range: Range = Range.FIVE_YEARS) {
        val assets = symbols.map { template.copy(symbol = it.uppercase()) }
        retrieve(assets.toList(), range = range)
    }

    /**
     * Retrieve historic end of day [PriceBar] for one or more assets
     *
     * @param assets
     */
    fun retrieve(assets: Collection<Asset>, range: Range = Range.FIVE_YEARS) {

        assets.forEach {
            val chart = client.executeRequest(
                ChartRequestBuilder()
                    .withChartRange(range)
                    .withSymbol(it.symbol)
                    .build()
            )
            handlePriceBar(it, chart)
        }
    }


    private fun getInstant(asset: Asset, date: String, minute: String?): Instant {
        return if (minute !== null) {
            val dt = LocalDateTime.parse("${date}T$minute")
            asset.exchange.getInstant(dt)
        } else {
            val d = LocalDate.parse(date)
            asset.exchange.getClosingTime(d)
        }
    }

    private fun handlePriceBar(asset: Asset, chart: List<Chart>) {
        chart.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)
            val now = getInstant(asset, it.date, it.minute)
            add(now, action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${timeline.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }


    private fun handleIntraday(asset: Asset, quotes: List<Intraday>) {
        quotes.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)
            val now = getInstant(asset, it.date, it.minute)
            add(now, action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${timeline.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }

}
