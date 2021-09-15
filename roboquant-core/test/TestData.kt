package org.roboquant

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.TradePrice
import org.roboquant.metrics.MetricResults
import org.roboquant.orders.MarketOrder
import java.io.File
import java.time.Instant

object TestData {

    fun usStock() = Asset("AAPL")

    fun euStock() = Asset("AF", currencyCode = "EUR", exchangeCode = "AEB")

    fun dataDir(): String {
        if (File("./data").isDirectory)
            return "./data/"
        else if (File("../data").isDirectory)
            return "../data/"
        throw Exception("cannot find data directory for testing")
    }


    fun euMarketOrder() = MarketOrder(euStock(), 10.0)

    fun usMarketOrder() = MarketOrder(usStock(), 10.0)

    private fun priceAction(asset:Asset = usStock()) = TradePrice(asset, 10.0)

    fun priceBar(asset:Asset = usStock()) = PriceBar(asset, 10.0, 11.0, 9.0, 10.0, 1000.0)

    fun time() = Instant.parse("2020-01-03T12:00:00Z")!!

    fun event(time: Instant = time()) = Event(listOf(priceAction()), time)

    fun event2(time: Instant = time()) = Event( listOf(priceAction(usStock()), priceAction(euStock())), time)

    fun metricInput(time: Instant = time()): Pair<Account, Event> {
        val account = Account()
        val moment = Event(listOf(priceAction()), time)
        return Pair(account, moment)
    }

    fun getMetrics(): MetricResults {
        return mapOf("key1" to 12.0, "key2" to 13.0)
    }

    fun getRunInfo(): RunInfo {
        return RunInfo("test", 1, 1, 10, Instant.now(), TimeFrame.FULL, Phase.MAIN)
    }


}