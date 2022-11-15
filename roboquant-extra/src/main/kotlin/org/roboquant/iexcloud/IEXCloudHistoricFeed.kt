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
@file:Suppress("DuplicatedCode")

package org.roboquant.iexcloud

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

/**
 * @see ChartRange
 */
typealias Range = ChartRange

/**
 * Feed of historic price data using IEXCloud Cloud as the data source.
 *
 * @property template The templates to use to instantiate Assets based on their symbol name
 * @param configure additional configuration
 *
 * @constructor
 */
class IEXCloudHistoricFeed(
    private val template: Asset = Asset("TEMPLATE"),
    configure: IEXCloudConfig.() -> Unit = {}
) : HistoricPriceFeed() {

    private val config = IEXCloudConfig()
    private val logger = Logging.getLogger(IEXCloudHistoricFeed::class)
    private val client: IEXCloudClient

    init {
        config.configure()
        client = IEXCloud.getClient(config)
    }

    /**
     * Retrieve historic end of day [PriceBar] for one or more symbols
     *
     * @param symbols
     */
    fun retrieveIntraday(vararg symbols: String) {
        symbols.forEach {
            val quote = client.executeRequest(
                IntradayRequestBuilder()
                    .withSymbol(it)
                    .build()
            )
            handleIntraday(template.copy(symbol = it), quote)
        }
    }

    /**
     * Retrieve historic end of day [PriceBar] for one or more symbols
     *
     * @param symbols
     * @param range the required range
     */
    fun retrieve(vararg symbols: String, range: Range = Range.FIVE_YEARS) {
        symbols.forEach {
            val chart = client.executeRequest(
                ChartRequestBuilder()
                    .withChartRange(range)
                    .withSymbol(it)
                    .build()
            )
            handlePriceBar(template.copy(symbol = it), chart)
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

    private fun handlePriceBar(asset: Asset, charts: List<Chart>) {
        charts.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume ?: Double.NaN)
            val now = getInstant(asset, it.date, it.minute)
            add(now, action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${timeline.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }

    private fun handleIntraday(asset: Asset, quotes: List<Intraday>) {
        quotes.filter { it.open !== null }.forEach {
            val action = PriceBar(asset, it.open, it.high, it.low, it.close, it.volume ?: Double.NaN)
            val now = getInstant(asset, it.date, it.minute)
            add(now, action)
        }
        logger.info { "Received data for $asset" }
        logger.info { "Total ${timeline.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }

}
