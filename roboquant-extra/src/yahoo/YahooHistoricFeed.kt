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

package org.roboquant.yahoo

import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.Timeframe
import org.roboquant.common.toUTC
import org.roboquant.feeds.HistoricPriceFeed
import org.roboquant.feeds.PriceBar
import yahoofinance.YahooFinance
import yahoofinance.histquotes.HistoricalQuote
import yahoofinance.histquotes.Interval
import java.util.*

/**
 * This feed uses historic data from Yahoo Finance API. Be aware this is not the most stable API and
 * might at any time stop working correctly.
 *
 * @property adjClose
 * @constructor Create new Yahoo finance feed
 */
class YahooHistoricFeed(private val adjClose: Boolean = true) : HistoricPriceFeed() {

    private val logger = Logging.getLogger(YahooHistoricFeed::class)


    /**
     * Retrieve historic [PriceBar] data from Yahoo Finance
     *
     * @param assets
     * @param timeframe
     * @param interval
     */
    fun retrieve(vararg assets: Asset, timeframe: Timeframe, interval: Interval = Interval.DAILY) {
        val c1 = GregorianCalendar.from(timeframe.start.toUTC())
        val c2 = GregorianCalendar.from(timeframe.end.toUTC())
        assets.forEach {
            val quotes = YahooFinance.get(it.symbol)
            val history = quotes.getHistory(c1, c2, interval)
            handle(it, history)
        }
        this.assets.addAll(assets)
    }


    /**
     * Retrieve historic [PriceBar] data from Yahoo Finance
     *
     * @param symbols
     * @param timeframe
     * @param interval
     */
    fun retrieve(vararg symbols: String, timeframe: Timeframe, interval: Interval = Interval.DAILY) {
        val assets = symbols.map { Asset(it) }.toTypedArray()
        retrieve(*assets, timeframe = timeframe, interval = interval)
    }

    // TODO validate time offset
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
