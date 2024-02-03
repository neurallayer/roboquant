/*
 * Copyright 2020-2024 Neural Layer
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

package org.roboquant.feeds.csv

import org.roboquant.common.Asset
import org.roboquant.common.TimeSpan
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.PriceQuote
import org.roboquant.feeds.TradePrice
import java.time.Instant

/**
 * Interface for time parsers that can use a config to support parsing logic
 */
fun interface PriceParser {

    /**
     * Initialize the parser based on the header. Default is to do nothing.
     */
    fun init(header: List<String>) {}

    /**
     * Return an [PriceAction] given the provided [line] of strings and [asset]
     */
    fun parse(line: List<String>, asset: Asset): PriceAction

}

/**
 * Parse lines and create PriceBar
 */
class PriceBarParser(
    private var open: Int = -1,
    private var high: Int = -1,
    private var low: Int = -1,
    private var close: Int = -1,
    private var volume: Int = -1,
    private var adjustedClose: Int = -1,
    private var priceAdjust: Boolean = false,
    private var autodetect: Boolean = true,
    private var timeSpan: TimeSpan? = null
) : PriceParser {

    private fun validate() {
        require(open != -1) { "No open-prices column" }
        require(low != -1) { "No low-prices column found" }
        require(high != -1) { "No high-prices column found" }
        require(close != -1) { "No close-prices column found" }
        if (priceAdjust) require(adjustedClose != -1) {
            "No adjusted close prices column found"
        }
    }

    override fun init(header: List<String>) {
        if (autodetect) {
            val notCapital = Regex("[^A-Z]")
            header.forEachIndexed { index, column ->
                when (column.uppercase().replace(notCapital, "")) {
                    "OPEN" -> open = index
                    "HIGH" -> high = index
                    "LOW" -> low = index
                    "CLOSE" -> close = index
                    "ADJCLOSE" -> adjustedClose = index
                    "ADJUSTEDCLOSE" -> adjustedClose = index
                    "VOLUME" -> volume = index
                    "VOL" -> volume = index
                }
            }
        }
        validate()
    }

    /**
     * Return an [Instant] given the provided [line] of strings and [asset]
     */
    override fun parse(line: List<String>, asset: Asset): PriceBar {
        val volume = if (volume != -1) {
            val str = line[volume]
            if (str.isBlank()) Double.NaN else str.toDouble()
        } else Double.NaN
        val action = PriceBar(
            asset,
            line[open].toDouble(),
            line[high].toDouble(),
            line[low].toDouble(),
            line[close].toDouble(),
            volume,
            timeSpan
        )
        if (priceAdjust) action.adjustClose(line[adjustedClose].toDouble())
        return action
    }

}

/**
 *Parse lines and create PriceQuote
 */
class PriceQuoteParser(
    private var ask: Int = -1,
    private var bid: Int = -1,
    private var bidVolume: Int = -1,
    private var askVolume: Int = -1,
    private var autodetect: Boolean = true
) : PriceParser {

    private fun validate() {
        require(ask != -1) { "No ask-prices column" }
        require(bid != -1) { "No bid-prices column found" }
    }

    override fun init(header: List<String>) {
        if (autodetect) {
            val notCapital = Regex("[^A-Z]")
            header.forEachIndexed { index, column ->
                when (column.uppercase().replace(notCapital, "")) {
                    "ASK" -> ask = index
                    "BID" -> bid = index
                    "ASKVOLUME" -> askVolume = index
                    "BIDVOLUME" -> bidVolume = index
                    "ASKSIZE" -> askVolume = index
                    "BIDSIZE" -> bidVolume = index
                }
            }
        }
        validate()
    }

    /**
     * Return an [Instant] given the provided [line] of strings and [asset]
     */
    override fun parse(line: List<String>, asset: Asset): PriceQuote {
        val volume1 = if (askVolume != -1) line[askVolume].toDouble() else Double.NaN
        val volume2 = if (bidVolume != -1) line[bidVolume].toDouble() else Double.NaN
        return PriceQuote(
            asset,
            line[ask].toDouble(),
            volume1,
            line[bid].toDouble(),
            volume2
        )
    }

}

/**
 * Parse lines and create Trade Prices
 */
class TradePriceParser(
    private var price: Int = -1,
    private var volume: Int = -1,
    private var autodetect: Boolean = true
) : PriceParser {

    private fun validate() {
        require(price != -1) { "No ask-prices column" }
    }

    override fun init(header: List<String>) {
        if (autodetect) {
            val notCapital = Regex("[^A-Z]")
            header.forEachIndexed { index, column ->
                when (column.uppercase().replace(notCapital, "")) {
                    "PRICE" -> price = index
                    "VOLUME" -> volume = index
                }
            }
        }
        validate()
    }

    /**
     * Return an [Instant] given the provided [line] of strings and [asset]
     */
    override fun parse(line: List<String>, asset: Asset): TradePrice {
        val volume = if (volume != -1) line[volume].toDouble() else Double.NaN
        return TradePrice(
            asset,
            line[price].toDouble(),
            volume
        )
    }

}
