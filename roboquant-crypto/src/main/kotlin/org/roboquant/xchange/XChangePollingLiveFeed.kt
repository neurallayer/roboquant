/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.xchange

import kotlinx.coroutines.delay
import org.knowm.xchange.Exchange
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.instrument.Instrument
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.LiveFeed
import org.roboquant.feeds.TradePrice

/**
 * Get live-streaming feeds from dozens of bitcoin exchanges. This feed uses the XChange library
 * to connect to the different exchanges to get the trade and order book feeds for a currency pair, using polling.
 *
 * XChange is a Java library providing a streamlined API for interacting with 60+ Bitcoin and Altcoin exchanges
 * providing a consistent interface for trading and accessing market data.
 *
 * See also https://knowm.org/open-source/xchange/
 * @constructor
 *
 * @param exchange the exchange instance to use. The connection to the provided exchange should already be live.
 */
class XChangePollingLiveFeed(
    exchange: Exchange,
) : LiveFeed() {

    private val logger = Logging.getLogger(XChangePollingLiveFeed::class)
    private val service = exchange.marketDataService
    private val exchangeName = exchange.exchangeSpecification?.exchangeName ?: exchange.toString()

    private val jobs = ParallelJobs()

    /**
     * Assets that are available to subscribe to
     */
    val availableAssets by lazy {
        val symbols = exchange.exchangeInstruments
        if (symbols == null) {
            logger.warn("No symbols available")
            listOf<Asset>()
        } else {
            symbols.map {
                val symbol = "${it.base.currencyCode}_${it.counter.currencyCode}"
                Asset(symbol, AssetType.CRYPTO, it.counter.currencyCode, exchangeName)
            }
        }
    }

    private val assetsMap = mutableMapOf<String, Asset>()

    /**
     * Assets that are subscribed to
     */
    val assets
        get() = assetsMap.values

    /**
     * Subscribe to live trade updates from the exchange. The resulting actions will be of the
     * type of [TradePrice] events.
     */
    fun subscribeTrade(vararg symbols: String, pollingDelayMillis: Int = 60_000) {
        for (symbol in symbols) assetsMap[symbol] = getAsset(symbol)

        jobs.add {
            var done = false
            while (!done) {
                for (symbol in symbols) {
                    val currencyPair = symbol.toCurrencyPair()!!
                    val cryptoPair: Instrument =
                        CurrencyPair(currencyPair.first.currencyCode, currencyPair.second.currencyCode)
                    val result = service.getTrades(cryptoPair)
                    for (trade in result.trades) {
                        println(trade)
                        val asset = assetsMap[symbol]!!
                        val item = TradePrice(asset, trade.price.toDouble(), trade.originalAmount.toDouble())
                        val now = trade.timestamp.toInstant()
                        val event = Event(listOf(item), now)
                        send(event)
                    }

                }
                delay(pollingDelayMillis.toLong())
                done = !isActive
            }
        }
    }

    /**
     * Stop all running jobs
     */
    override fun close() = jobs.cancelAll()

    /**
     * Get an asset based on a cryptocurrency pair.
     *
     * @return
     */
    private fun getAsset(symbol: String): Asset {
        val currencyPair = symbol.toCurrencyPair()!!
        return Asset(symbol, AssetType.CRYPTO, currencyPair.second.currencyCode, exchangeName)
    }

}