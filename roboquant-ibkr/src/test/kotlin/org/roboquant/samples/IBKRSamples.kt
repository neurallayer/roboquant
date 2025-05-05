/*
 * Copyright 2020-2025 Neural Layer
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

import org.roboquant.brokers.FixedExchangeRates
import org.roboquant.common.*
import org.roboquant.common.PriceItem
import org.roboquant.feeds.filter
import org.roboquant.ibkr.IBKR
import org.roboquant.ibkr.IBKRBroker
import org.roboquant.ibkr.IBKRHistoricFeed
import org.roboquant.ibkr.IBKRLiveFeed
import org.roboquant.strategies.EMACrossover
import kotlin.test.Test

internal class IBKRSamples {

    @Test
    internal fun broker() {
        Config.exchangeRates = FixedExchangeRates(Currency.USD, Currency.EUR to 1.1)
        val broker = IBKRBroker()
        val account = broker.sync()

        println(account)
        Thread.sleep(5000)
        println(account.positions.keys)
        broker.disconnect()
    }

    @Test
    internal fun closePosition() {
        val broker = IBKRBroker()
        val account = broker.sync()
        println(account)

        // Place a new market sell order
        val asset = account.positions.keys.first()
        val position = account.positions.getValue(asset)
        val order = Order(asset, -position.size, position.mktPrice)
        broker.placeOrders(listOf(order))
        Thread.sleep(10_000)
        println(account)
        broker.disconnect()
    }

    @Test
    internal fun showAccount() {

        // If you have multiple currencies in your trading-account, you need to set up exchange rates
        // Config.exchangeRates = IBKRExchangeRates()

        // Get the account object from the broker instance
        val broker = IBKRBroker()
        val account = broker.sync()

        // Print the full summary of the account
        println(account)

        // Disconnect
        broker.disconnect()
    }



    @Test
    internal fun simplePaperTrade() {
        // Lets trade these 3 tech stock
        val tsla = Stock("TSLA")
        val msft = Stock("MSFT")
        val googl = Stock("GOOGL")

        // Link the asset to an IBKR contract-id.
        IBKR.register(76792991, tsla)
        IBKR.register(272093, msft)
        IBKR.register(208813719, googl)

        val broker = IBKRBroker()
        Config.exchangeRates = broker.exchangeRates
        val account = broker.sync()
        println(account)

        val feed = IBKRLiveFeed { client = 3 }
        feed.subscribe(tsla, msft, googl)

        val strategy = EMACrossover.PERIODS_12_26
        org.roboquant.run(feed, strategy,timeframe = Timeframe.next(2.hours))

        feed.disconnect()
        broker.disconnect()
        println("done")
    }

    @Test
    internal fun liveFeedEU() {
        val feed = IBKRLiveFeed()
        val asset = Stock("ABN", Currency.EUR)
        feed.subscribe(listOf(asset))
        val tf = Timeframe.next(10.minutes)
        val data = feed.filter<PriceItem>(tf) {
            println(it)
            true
        }
        println(data.size)
        feed.disconnect()
    }

    @Test
    internal fun liveFeedUS() {
        val feed = IBKRLiveFeed()
        val asset = Stock("TSLA")
        feed.subscribe(listOf(asset))
        val tf = Timeframe.next(1.minutes)
        val data = feed.filter<PriceItem>(tf) {
            println(it)
            true
        }
        println(data.size)
        feed.disconnect()
    }

    @Test
    internal fun historicFeed() {
        val feed = IBKRHistoricFeed()

        // This assumes you have a valid market subscription for European stocks
        val symbols = listOf("ABN", "ASML", "KPN")
        val assets = symbols.map { Stock(it, Currency.EUR) }
        feed.retrieve(assets)
        feed.waitTillRetrieved()
        println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
        feed.disconnect()
    }

    @Test
    internal fun historicFeedUS() {
        val feed = IBKRHistoricFeed()

        // This assumes you have a valid market subscription for European stocks
        val symbols = listOf("TSLA", "GOOGL", "JPM")
        val assets = symbols.map { Stock(it) }
        feed.retrieve(assets)
        feed.waitTillRetrieved()
        println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
        feed.disconnect()
    }

    @Test
    internal fun historicFeed2() {
        val feed = IBKRHistoricFeed()

        // This assumes you have a valid market subscription for European stocks
        val symbols = listOf("ABN", "ASML", "KPN")
        val assets = symbols.map { Stock(it, Currency.EUR) }
        feed.retrieve(assets, duration = "5 D", barSize = "1 min")
        feed.waitTillRetrieved()
        println("historic feed with ${feed.timeline.size} events and ${feed.assets.size} assets")
        feed.disconnect()
    }



}
