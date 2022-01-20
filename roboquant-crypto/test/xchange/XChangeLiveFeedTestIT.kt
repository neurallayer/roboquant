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

package org.roboquant.xchange

import info.bitrich.xchangestream.bitstamp.v2.BitstampStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import org.roboquant.Roboquant
import org.roboquant.common.Timeframe
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import org.roboquant.common.minutes
import kotlin.test.assertEquals


internal class XChangeLiveFeedTestIT {

    @Test
    fun xchangeFeedIT() {
        System.getProperty("TEST_XCHANGE") ?: return

        val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange::class.java)
        exchange.connect().blockingAwait()
        val feed = XChangeLiveFeed(exchange)

        // Mix three kind of price actions in a single feed
        feed.subscribeTrade(Pair("BTC", "USD"))
        feed.subscribeOrderBook(Pair("BAT", "USD"))
        feed.subscribeTicker(Pair("ETH", "BTC"))

        assertEquals(3, feed.assets.size)

        val strategy = EMACrossover.shortTerm()
        val roboquant = Roboquant(strategy, ProgressMetric())

        /// Run it for 5 minutes
        val timeframe = Timeframe.next(5.minutes)
        roboquant.run(feed, timeframe)
        exchange.disconnect().blockingAwait()
    }

}
