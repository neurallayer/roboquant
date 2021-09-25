package org.roboquant.xchange

import info.bitrich.xchangestream.bitstamp.v2.BitstampStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import org.roboquant.Roboquant
import org.roboquant.common.TimeFrame
import org.roboquant.metrics.ProgressMetric
import org.roboquant.strategies.EMACrossover
import org.junit.Test
import kotlin.test.assertEquals


internal class XChangeFeedTestIT {

    @Test
    fun xchangeFeedIT() {
        System.getProperty("xchange") ?: return

        val exchange = StreamingExchangeFactory.INSTANCE.createExchange(BitstampStreamingExchange::class.java)
        exchange.connect().blockingAwait()
        val feed = XChangeFeed(exchange)

        // Mix three kind of price actions in a single feed
        feed.subscribeTrade(Pair("BTC", "USD"))
        feed.subscribeOrderBook(Pair("BAT", "USD"))
        feed.subscribeTicker(Pair("ETH", "BTC"))

        assertEquals(3, feed.assets.size)

        val strategy = EMACrossover.shortTerm()
        val roboquant = Roboquant(strategy, ProgressMetric())

        /// Run it for 1 minute
        val timeFrame = TimeFrame.nextMinutes(1)
        roboquant.run(feed, timeFrame)
        exchange.disconnect().blockingAwait()
    }

}
