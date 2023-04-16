/*
 * Copyright 2020-2023 Neural Layer
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

package org.roboquant.samples

import org.roboquant.Roboquant
import org.roboquant.brokers.Account
import org.roboquant.brokers.fee
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Config
import org.roboquant.common.Size
import org.roboquant.common.days
import org.roboquant.common.getBySymbol
import org.roboquant.feeds.AvroFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.csv.CSVFeed
import org.roboquant.loggers.MemoryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.MetricResults
import org.roboquant.metrics.PNLMetric
import org.roboquant.metrics.ProgressMetric
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Order
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.NoSignalStrategy
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.ta.AtrPolicy
import org.roboquant.ta.BettingAgainstBetaPolicy
import org.roboquant.ta.TaLibMetric
import org.roboquant.ta.TaLibSignalStrategy


private fun beta() {
    val feed = CSVFeed("/data/assets/stock-market/stocks/")
    val market = CSVFeed("/data/assets/stock-market/market/")
    feed.merge(market)
    val strategy = NoSignalStrategy()
    val marketAsset = feed.assets.getBySymbol("SPY")

    val policy = BettingAgainstBetaPolicy(feed.assets, marketAsset, maxPositions = 10)
    policy.recording = true
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), policy = policy, logger = logger)
    roboquant.run(feed)
    println(logger.summary())
    println(roboquant.broker.account.summary())

}

private fun beta2() {
    val feed = CSVFeed("/data/assets/us-stocks/Stocks") {
        fileExtension = ".us.txt"
    }
    val market = CSVFeed("/data/assets/us-stocks/ETFs") {
        fileExtension = ".us.txt"
        filePattern = "spy.us.txt"

    }
    feed.merge(market)
    val strategy = NoSignalStrategy()
    val marketAsset = feed.assets.getBySymbol("SPY")
    val policy = BettingAgainstBetaPolicy(feed.assets, marketAsset, 60.days, maxPositions = 10)
    policy.recording = true
    val logger = MemoryLogger()
    val roboquant = Roboquant(strategy, ProgressMetric(), PNLMetric(), policy = policy, logger = logger)
    roboquant.run(feed)
    println(logger.summary())
    println(roboquant.broker.account.summary())
    println(roboquant.broker.account.trades.fee)

}

private fun macd() {
    val strategy = TaLibSignalStrategy(35) { asset, prices ->
        val (_, _, diff) = macd(prices, 12, 26, 9)
        val (_, _, diff2) = macd(prices, 12, 26, 9, 1)
        when {
            diff < 0.0 && diff2 > 0.0 -> Signal(asset, Rating.BUY)
            diff > 0.0 && diff2 < 0.0 -> Signal(asset, Rating.SELL)
            else -> null
        }
    }

    val rq = Roboquant(strategy)
    val feed = AvroFeed.sp500()
    rq.run(feed)
    println(rq.broker.account.summary())

}

private fun customPolicy() {

    /**
     * Custom Policy that extends the FlexPolicy and captures the ATR (Average True Range) using the TaLibMetric. It
     * then uses the ATR to set the limit amount of a LimitOrder.
     */
    class SmartLimitPolicy(private val atrPercentage: Double = 0.02, private val atrPeriod: Int) : FlexPolicy() {

        // use TaLibMetric to calculate the ATR values
        private val atr = TaLibMetric("atr", atrPeriod + 1) { atr(it, atrPeriod) }
        private var atrMetrics: MetricResults = emptyMap()

        override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
            // Update the metrics and store the results, so we have them available when the
            // createOrder is invoked.
            atrMetrics = atr.calculate(account, event)

            // Call the regular FlexPolicy processing
            return super.act(signals, account, event)
        }

        /**
         * Override the default behavior of creating a simple MarkerOrder. Create limit BUY and SELL orders with the
         * actual limit based on the ATR of the underlying asset.
         */
        override fun createOrder(signal: Signal, size: Size, price: Double): Order? {
            val metricName = "atr.${signal.asset.symbol.lowercase()}"
            val value = atrMetrics[metricName]
            return if (value != null) {
                val direction = if (size > 0) 1 else -1
                val limit = price - direction * value * atrPercentage
                LimitOrder(signal.asset, size, limit)
            } else {
                null
            }
        }

        override fun reset() {
            atr.reset()
            super.reset()
        }
    }

    val roboquant = Roboquant(EMAStrategy.PERIODS_12_26, AccountMetric(), policy = SmartLimitPolicy(atrPeriod = 5))
    val feed = AvroFeed.sp500()
    roboquant.run(feed)
    println(roboquant.broker.account.summary())
}

private fun atrPolicy() {
    val strategy = EMAStrategy.PERIODS_5_15
    val policy = AtrPolicy(10, 6.0, 3.0, orderPercentage = 0.02, atRisk = Double.NaN, shorting = true)
    val broker = SimBroker(accountModel = MarginAccount(minimumEquity = 50_000.0))
    val rq = Roboquant(strategy, broker = broker, policy = policy)

    val feed = AvroFeed.sp500()
    rq.run(feed)
    println(rq.broker.account.summary())
}


@Suppress("KotlinConstantConditions")
fun main() {
    Config.printInfo()

    when ("ATR") {
        "CUSTOM" -> customPolicy()
        "BETA" -> beta()
        "BETA2" -> beta2()
        "MACD" -> macd()
        "ATR" -> atrPolicy()
    }

}

