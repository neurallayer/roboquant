@file:Suppress("KotlinConstantConditions")
package samples

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.Roboquant
import org.roboquant.binance.BinanceHistoricFeed
import org.roboquant.binance.Interval
import org.roboquant.brokers.sim.MarginBuyingPower
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.avro.AvroUtil
import org.roboquant.feeds.filter
import org.roboquant.jupyter.Chart
import org.roboquant.jupyter.PriceBarChart
import org.roboquant.metrics.AccountSummary
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMACrossover
import org.roboquant.xchange.XChangePollingLiveFeed
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals


fun recordBinanceFeed() {
    val feed = BinanceHistoricFeed()
    val timeframe = Timeframe.parse("2019-01-01", "2022-01-01")
    // val timeframe = Timeframe.parse("2019-05-01", "2019-05-02")
    for (period in timeframe.split(12.hours)) {
        feed.retrieve("BTCUSDC", "ETHUSDC", timeframe = period, interval = Interval.ONE_MINUTE)
        println("$period ${feed.timeline.size}")
        Thread.sleep(2000)
    }

    val userHomeDir = System.getProperty("user.home")
    val fileName = "$userHomeDir/tmp/crypto.avro"
    AvroUtil.record(feed, fileName)

    // Some sanity checks
    val feed2 = AvroFeed(fileName)
    require(feed2.assets.size == feed.assets.size)
    require(feed2.timeline.size == feed.timeline.size)

    println("done")
}

fun readBianceFeed() {
    println("starting")
    val t = measureTimeMillis {
        val userHomeDir = System.getProperty("user.home")
        val fileName = "$userHomeDir/tmp/crypto.avro"
        val feed = AvroFeed(fileName, useIndex = true)
        val initialDeposit = Amount("USDC", 100_000).toWallet()
        val buyingPower = MarginBuyingPower()
        val policy = DefaultPolicy(shorting = true)
        val broker = SimBroker(initialDeposit, buyingPowerModel = buyingPower)
        val roboquant = Roboquant(EMACrossover(), AccountSummary(), broker = broker, policy = policy)
        roboquant.run(feed)
        roboquant.broker.account.summary().log()
    }
    println(t)
}


fun readBianceFeed2() {
    val userHomeDir = System.getProperty("user.home")
    val fileName = "$userHomeDir/tmp/crypto.avro"
    val feed = AvroFeed(fileName)
    Chart.maxSamples = 100_000
    val chart = PriceBarChart(feed, feed.assets.first())
    println( measureTimeMillis {
        chart.asHTML()
    })
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

    when("READ") {
        "RECORD" -> recordBinanceFeed()
        "READ" -> readBianceFeed()
        "READ2" -> readBianceFeed2()
        "XCHANGE" -> xchangeFeed ()
    }
}