@file:Suppress("KotlinConstantConditions")
package samples

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.binance.BinanceHistoricFeed
import org.roboquant.binance.Interval
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.xchange.XChangePollingLiveFeed
import kotlin.test.assertEquals



fun recordBinanceFeed() {
    val feed = BinanceHistoricFeed()
    val timeframe = Timeframe.parse("2019-02-01", "2019-02-02")
    for (period in timeframe.split(12.hours)) {
        println(period)
        feed.retrieve("BTCUSDC", "ETHUSDC", timeframe = period, interval = Interval.ONE_MINUTE)
        println(feed.timeline.size)
        Thread.sleep(2000)
    }
    feed.close()
}


fun xchangeFeed() {

    val exchange = ExchangeFactory.INSTANCE.createExchange(BitstampExchange::class.java)
    val feed = XChangePollingLiveFeed(exchange)
    feed.availableAssets.summary().print()

    feed.subscribeTrade("BTC_USD", pollingDelayMillis = 30_000)
    println("Subscribed")
    assertEquals("BTC_USD", feed.assets.first().symbol)

    /// Run it for 2 minutes
    val timeframe = Timeframe.next(2.minutes)
    val result = feed.filter<PriceAction>(timeframe = timeframe)
    feed.close()

    assertEquals(AssetType.CRYPTO, result.first().second.asset.type)

}

fun main() {

    when("RECORD") {
        "RECORD" -> recordBinanceFeed()
        "XCHANGE" -> xchangeFeed ()
    }
}