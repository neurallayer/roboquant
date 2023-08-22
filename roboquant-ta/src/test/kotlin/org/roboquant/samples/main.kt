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
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Config
import org.roboquant.common.Size
import org.roboquant.common.percent
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceAction
import org.roboquant.metrics.AccountMetric
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Order
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import org.roboquant.ta.*
import org.ta4j.core.indicators.bollinger.BollingerBandFacade
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedUpIndicatorRule


private fun macd() {
    val strategy = TaLibSignalStrategy { asset, prices ->
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




private fun ta4j() {
    // How big a look-back period should we use
    val period = 20

    val strategy = Ta4jStrategy(maxBarCount = period)

    strategy.buy { series ->
        val b = BollingerBandFacade(series, period, 1.0)
        val closePrice = ClosePriceIndicator(series)
        CrossedUpIndicatorRule(closePrice, b.upper())
    }

    strategy.sell { series ->
        val b = BollingerBandFacade(series, period, 1.0)
        val closePrice = ClosePriceIndicator(series)
        CrossedUpIndicatorRule(closePrice, b.lower())
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
    class SmartLimitPolicy(private val atrPercentage: Double = 200.percent, private val atrPeriod: Int) : FlexPolicy() {

        // use TaLibMetric to calculate the ATR values
        private val atr = TaLibMetric { mapOf("atr" to atr(it, atrPeriod)) }
        private var atrMetrics = emptyMap<String, Double>()

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
        override fun createOrder(signal: Signal, size: Size, priceAction: PriceAction): Order? {
            val metricName = "atr.${signal.asset.symbol.lowercase()}"
            val value = atrMetrics[metricName]
            val price = priceAction.getPrice(config.priceType)
            return if (value != null) {
                val limit = price - size.sign * value * atrPercentage
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
    val policy = AtrPolicy(10, 6.0, 3.0, null) {
        orderPercentage = 0.02
        shorting = true
    }
    val broker = SimBroker(accountModel = MarginAccount(minimumEquity = 50_000.0))
    val rq = Roboquant(strategy, broker = broker, policy = policy)

    val feed = AvroFeed.sp500()
    rq.run(feed)
    println(rq.broker.account.summary())
}

@Suppress("CyclomaticComplexMethod")
private fun tenkan() {

    /**
     * Midpoint Moving Average
     */
    fun TaLib.mma(priceBarSeries: PriceBarSeries, period: Int): Double {
        return (max(priceBarSeries.high, period) + min(priceBarSeries.low, period)) / 2.0
    }

    /**
     * Tenkan indicator
     */
    fun TaLib.tenkan(priceBarSeries: PriceBarSeries) = mma(priceBarSeries, 9)

    /**
     * Kijun indicator
     */
    fun TaLib.kijun(priceBarSeries: PriceBarSeries) = mma(priceBarSeries, 26)

    val strategy = TaLibSignalStrategy {asset, series ->
        val tenkan = tenkan(series)
        val kijun = kijun(series)
        when {
            tenkan > kijun && series.open.last() < tenkan && series.close.last() > tenkan -> Signal(asset, Rating.BUY)
            tenkan < kijun && series.close.last() < kijun -> Signal(asset, Rating.SELL)
            else -> null
        }
    }

    // val broker = SimBroker(pricingEngine = SpreadPricingEngine(1.5))
    // val rq = Roboquant(strategy, AccountMetric(), broker = broker)
    // val feed = AvroFeed.forex()

    val rq = Roboquant(strategy, AccountMetric())
    val feed = AvroFeed.sp500()

    println(feed.timeframe)
    rq.run(feed)
    println(rq.broker.account.summary())

}

@Suppress("KotlinConstantConditions")
fun main() {
    Config.printInfo()

    when ("TENKAN") {
        "CUSTOM" -> customPolicy()
        "MACD" -> macd()
        "ATR" -> atrPolicy()
        "TA4J" -> ta4j()
        "TENKAN" -> tenkan()
    }

}

