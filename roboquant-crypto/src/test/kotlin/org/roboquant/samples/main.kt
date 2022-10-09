/*
 * Copyright 2020-2022 Neural Layer
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

@file:Suppress("KotlinConstantConditions")

package org.roboquant.samples

import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.bitstamp.BitstampExchange
import org.roboquant.Roboquant
import org.roboquant.binance.BinanceHistoricFeed
import org.roboquant.binance.Interval
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.*
import org.roboquant.feeds.PriceAction
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.avro.AvroUtil
import org.roboquant.feeds.filter
import org.roboquant.metrics.AccountMetric
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMAStrategy
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

fun readBinanceFeed() {
    println("starting")
    val t = measureTimeMillis {
        val userHomeDir = System.getProperty("user.home")
        val fileName = "$userHomeDir/tmp/crypto.avro"
        val feed = AvroFeed(fileName, useIndex = true)
        val initialDeposit = Amount("USDC", 100_000).toWallet()
        val buyingPower = MarginAccount()
        val policy = DefaultPolicy(shorting = true)
        val broker = SimBroker(initialDeposit, accountModel = buyingPower)
        val roboquant = Roboquant(EMAStrategy(), AccountMetric(), broker = broker, policy = policy)
        roboquant.run(feed)
        println(roboquant.broker.account.summary())
    }
    println(t)
}

fun xchangeFeed() {

    val exchange = ExchangeFactory.INSTANCE.createExchange(BitstampExchange::class.java)
    val feed = XChangePollingLiveFeed(exchange)
    println(feed.availableAssets.summary())

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

    when ("READ") {
        "RECORD" -> recordBinanceFeed()
        "READ" -> readBinanceFeed()
        "XCHANGE" -> xchangeFeed()
    }
}