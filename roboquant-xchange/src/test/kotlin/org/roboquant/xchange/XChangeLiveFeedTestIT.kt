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

package org.roboquant.xchange

import info.bitrich.xchangestream.bitstamp.v2.BitstampStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.common.AssetType
import org.roboquant.common.Timeframe
import org.roboquant.common.minutes
import org.roboquant.common.summary
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.filter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class XChangeLiveFeedTestIT {

    @Test
    fun xchangeFeedIT() {
        System.getProperty("TEST_XCHANGE") ?: return

        val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange::class.java)
        exchange.connect().blockingAwait()
        val feed = XChangeLiveFeed(exchange)
        println(feed.availableAssets.summary())

        // Mix three kind of price actions in a single feed
        feed.subscribeTrade(Pair("BTC", "USD"))
        feed.subscribeOrderBook(Pair("BAT", "USD"))
        feed.subscribeTicker("ETH_BTC")

        assertEquals(3, feed.assets.size)

        /// Run it for 2 minutes
        val timeframe = Timeframe.next(2.minutes)
        val result = feed.filter<PriceItem>(timeframe = timeframe)
        exchange.disconnect().blockingAwait()

        assertEquals(AssetType.CRYPTO, result.first().second.asset.type)
    }

    @Test
    fun xchangeFeedPollingIT() {
        System.getProperty("TEST_XCHANGE") ?: return

        val exchange = ExchangeFactory.INSTANCE.createExchange(BitstampExchange::class.java)
        val feed = XChangePollingLiveFeed(exchange)
        println(feed.availableAssets.summary())

        feed.subscribeTrade("BTC_USD", pollingDelayMillis = 30_000)
        println("Subscribed")
        assertEquals("BTC_USD", feed.assets.first().symbol)

        /// Run it for 2 minutes
        val timeframe = Timeframe.next(2.minutes)
        val result = feed.filter<PriceItem>(timeframe = timeframe)
        feed.close()

        assertEquals(AssetType.CRYPTO, result.first().second.asset.type)

    }

}
