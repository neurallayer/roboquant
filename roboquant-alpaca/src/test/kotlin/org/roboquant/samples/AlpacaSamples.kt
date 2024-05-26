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

package org.roboquant.samples

import org.roboquant.alpaca.AlpacaBroker
import org.roboquant.alpaca.AlpacaHistoricFeed
import org.roboquant.alpaca.AlpacaLiveFeed
import org.roboquant.alpaca.PriceActionType
import org.roboquant.common.*
import org.roboquant.feeds.applyEvents
import org.roboquant.feeds.toList
import org.roboquant.orders.MarketOrder
import java.time.Instant
import kotlin.test.Ignore
import kotlin.test.Test

internal class AlpacaSamples {

    private val symbols = arrayOf(
        "AAPL",
        "IBM",
        "JPM",
        "MSFT",
        "TSLA",
        "GOOGL",
        "AMZN",
        "BRK.A",
        "V",
        "BABA",
        "NVDA",
        "JNJ",
        "TSM",
        "WMT"
    )

    @Test
    @Ignore
    internal fun alpacaBroker() {
        val broker = AlpacaBroker()
        val account = broker.sync()
        println(account)
    }



    @Test
    @Ignore
    internal fun alpacaLiveFeed() {
        val feed = AlpacaLiveFeed()
        feed.subscribeStocks(*symbols, type = PriceActionType.QUOTE)
        var events = 0L
        var items = 0L
        var delays = 0L
        val tf = Timeframe.next(1.minutes)
        feed.applyEvents(tf) {
            if (it.items.isNotEmpty()) {
                events++
                items += it.items.size
                delays += Instant.now().toEpochMilli() - it.time.toEpochMilli()
            }
        }
        feed.close()
        if (events > 0) {
            val avgDelay = delays / events.toDouble()
            println("avg-delay=${avgDelay}ms events=$events items=$items")
        } else {
            println("no events, perhaps outside trading hours")
        }
    }


    @Test
    @Ignore
    internal fun alpacaHistoricFeed2() {
        val symbols = arrayOf(
            "AAPL",
            "IBM",
            "JPM",
            "MSFT",
            "TSLA",
            "GOOGL",
            "AMZN",
            "BRK.A",
            "V",
            "BABA",
            "NVDA",
            "JNJ",
            "TSM",
            "WMT"
        )
        val feed = AlpacaHistoricFeed()

        // We get the data for the last 200 days. The minus 15.minutes is to make sure we only request data that
        // the free subscriptions are entitled to and not the latest 15 minutes.
        val tf = Timeframe.past(200.days) - 15.minutes
        feed.retrieveStockPriceBars(symbols.joinToString(","), timeframe = tf)
        val events = feed.toList()

        with(events) {
            println("events=$size start=${first().time} last=${last().time} symbols=${feed.assets.symbols.toList()}")
        }

    }

    @Test
    @Ignore
    internal fun alpacaHistoricFeed3() {
        val feed = AlpacaHistoricFeed()

        // We get minute data
        val tf = Timeframe.parse("2016-01-01", "2024-05-05")
        feed.retrieveStockPriceBars("AAPL", timeframe = tf, "1Min")
        println(feed)
    }

    @Test
    @Ignore
    internal fun singleOrder() {
        val broker = AlpacaBroker { extendedHours = false }
        repeat(10) {
            val order = MarketOrder(Asset("IBM"), Size.ONE)
            broker.place(listOf(order))
            Thread.sleep(5_000)
            val account = broker.sync()
            println(account)
        }
    }

}
