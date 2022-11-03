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

package org.roboquant.yahoo

import org.roboquant.common.*
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import yahoofinance.YahooFinance
import yahoofinance.histquotes.HistoricalQuote
import java.util.*

/**
 * Interval for Yahoo feeds, options being DAILY, WEEKLY or MONTHLY
 */
typealias Interval = yahoofinance.histquotes.Interval

/**
 * This feed uses historic data from Yahoo Finance API. Be aware this is not the most stable API and
 * might at any time stop working correctly.
 *
 * @property adjClose should we use adjusted close
 * @constructor Create new Yahoo finance feed
 */
class YahooHistoricFeed(private val adjClose: Boolean = true) : HistoricPriceFeed() {

    private val logger = Logging.getLogger(YahooHistoricFeed::class)

    /**
     * Retrieve historic [PriceBar] data from Yahoo Finance for the provided [symbols], [timeframe] and [interval].
     */
    fun retrieve(
        vararg symbols: String,
        timeframe: Timeframe = Timeframe.past(1.years),
        interval: Interval = Interval.DAILY
    ) {
        val c1 = GregorianCalendar.from(timeframe.start.toUTC())
        val c2 = GregorianCalendar.from(timeframe.end.toUTC())

        val quotes = YahooFinance.get(symbols, c1, c2, interval)
        for (value in quotes.values) {
            val asset = Asset(value.symbol, AssetType.STOCK, value.currency, value.stockExchange)
            handle(asset, value.history)
            assets.add(asset)
        }
    }


    private fun handle(asset: Asset, quotes: List<HistoricalQuote>) {

        quotes.forEach {
            val action = if (adjClose)
                PriceBar.fromAdjustedClose(asset, it.open, it.high, it.low, it.close, it.adjClose, it.volume)
            else
                PriceBar(asset, it.open, it.high, it.low, it.close, it.volume)

            val now = it.date.toInstant()
            add(now, action)
        }

        logger.info { "Received data for $asset" }
        logger.info { "Total ${timeline.size} steps from ${timeline.first()} to ${timeline.last()}" }
    }

}
