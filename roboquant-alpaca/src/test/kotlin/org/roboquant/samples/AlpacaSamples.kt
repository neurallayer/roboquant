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

import org.roboquant.Roboquant
import org.roboquant.alpaca.AlpacaBroker
import org.roboquant.alpaca.AlpacaHistoricFeed
import org.roboquant.alpaca.AlpacaLiveFeed
import org.roboquant.alpaca.PriceActionType
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.feeds.toList
import org.roboquant.loggers.InfoLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.MarketOrder
import org.roboquant.strategies.EMAStrategy
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
        println(account.fullSummary())
    }

    @Test
    @Ignore
    internal fun alpacaPaperTradeStocks() {
        val broker = AlpacaBroker()
        val account = broker.sync()
        println(account.fullSummary())

        val feed = AlpacaLiveFeed()

        // Lets pick 10 random stock symbols to trade
        val symbols = feed.availableStocks.random(10).symbols
        feed.subscribeStocks(*symbols)

        val strategy = EMAStrategy(3, 5)
        val roboquant = Roboquant(strategy, AccountMetric(), ProgressMetric(), broker = broker, logger = InfoLogger())
        val tf = Timeframe.next(60.minutes)
        val account2 = roboquant.run(feed, tf)
        feed.close()

        println(account2.fullSummary())
    }


    @Test
    @Ignore
    internal fun alpacaTradeCrypto() {
        val feed = AlpacaLiveFeed()
        val symbols = feed.availableCrypto.random(10).symbols
        println(symbols.toList())

        feed.subscribeCrypto(*symbols)
        val strategy = EMAStrategy.PERIODS_5_15
        val roboquant = Roboquant(strategy, ProgressMetric())
        val tf = Timeframe.next(10.minutes)
        val account = roboquant.run(feed, tf)
        feed.close()
        println(account.summary())
    }

    @Test
    @Ignore
    internal fun alpacaLiveFeed() {
        val feed = AlpacaLiveFeed()
        feed.subscribeStocks(*symbols, type = PriceActionType.QUOTE)
        feed.filter<PriceAction>(Timeframe.next(5.minutes)) {
            println(it)
            false
        }
        feed.close()
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
        feed.retrieveStockPriceBars(*symbols, timeframe = tf)
        val events = feed.toList()

        with(events) {
            println("events=$size start=${first().time} last=${last().time}")
        }

    }

    @Test
    @Ignore
    internal fun singleOrder() {
        val broker = AlpacaBroker {
            extendedHours = true
        }
        val order = MarketOrder(Asset("IBM"), Size.ONE)
        broker.place(listOf(order))
        Thread.sleep(5000)
        val account = broker.sync()
        println(account.fullSummary())
    }

}
