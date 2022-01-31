package samples

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.common.AssetType
import org.roboquant.common.Timeframe
import org.roboquant.common.minutes
import org.roboquant.common.summary
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.filter
import org.roboquant.xchange.XChangePollingLiveFeed
import kotlin.test.assertEquals


fun main() {

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

