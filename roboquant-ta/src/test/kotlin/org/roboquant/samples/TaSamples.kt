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

package org.roboquant.samples

import org.roboquant.run
import org.roboquant.brokers.Account
import org.roboquant.brokers.sim.MarginAccount
import org.roboquant.brokers.sim.SimBroker
import org.roboquant.common.Size
import org.roboquant.common.percent
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceItem
import org.roboquant.feeds.random.RandomWalk
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Instruction
import org.roboquant.strategies.FlexConverter
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.Signal
import org.roboquant.ta.*
import org.ta4j.core.indicators.bollinger.BollingerBandFacade
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedUpIndicatorRule
import kotlin.test.Ignore
import kotlin.test.Test

internal class TaSamples {

    @Test
    @Ignore
    internal fun macd() {
        val strategy = TaLibSignalStrategy { asset, prices ->
            val (_, _, diff) = macd(prices, 12, 26, 9)
            val (_, _, diff2) = macd(prices, 12, 26, 9, 1)
            when {
                diff < 0.0 && diff2 > 0.0 -> Signal.buy(asset)
                diff > 0.0 && diff2 < 0.0 -> Signal.sell(asset)
                else -> null
            }
        }

        val feed = RandomWalk.lastYears(5)
        val account = run(feed, strategy)
        println(account)
    }

    @Test
    @Ignore
    internal fun ta4j() {
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

        val feed = RandomWalk.lastYears(5)
        val account = run(feed, strategy)
        println(account)
    }

    @Test
    @Ignore
    internal fun customPolicy() {

        /**
         * Custom SignalConverter that extends the FlexConverter and captures the ATR (Average True Range) using the
         * TaLibMetric. It then uses the ATR to set the limit amount of a LimitOrder.
         */
        class SmartLimitConverter(private val atrPercentage: Double = 200.percent, private val atrPeriod: Int) :
            FlexConverter() {

            // use TaLibMetric to calculate the ATR values
            private val atr = TaLibMetric { mapOf("atr" to atr(it, atrPeriod)) }
            private var atrMetrics = emptyMap<String, Double>()

            override fun convert(signals: List<Signal>, account: Account, event: Event): List<Instruction> {
                // Update the metrics and store the results, so we have them available when the
                // createOrder is invoked.
                atrMetrics = atr.calculate(account, event)

                // Call the regular FlexConverter processing
                return super.convert(signals, account, event)
            }

            /**
             * Override the default behavior of creating a simple MarkerOrder. Create limit BUY and SELL orders with the
             * actual limit based on the ATR of the underlying asset.
             */
            override fun createOrder(signal: Signal, size: Size, priceItem: PriceItem): Instruction? {
                val metricName = "atr.${signal.asset.symbol.lowercase()}"
                val value = atrMetrics[metricName]
                val price = priceItem.getPrice(config.priceType)
                return if (value != null) {
                    val limit = price - size.sign * value * atrPercentage
                    LimitOrder(signal.asset, size, limit)
                } else {
                    null
                }
            }

        }


        val feed = RandomWalk.lastYears(5)
        val strategy = EMACrossover.PERIODS_12_26
        strategy.signalConverter = SmartLimitConverter(atrPeriod = 5)
        val account = run(feed, EMACrossover.PERIODS_12_26)
        println(account)
    }

    @Test
    @Ignore
    internal fun atrPolicy() {
        val strategy = EMACrossover.PERIODS_5_15
        val converter = AtrSignalConverter(10, 6.0, 3.0, null) {
            orderPercentage = 0.02
            shorting = true
        }
        strategy.signalConverter = converter
        val broker = SimBroker(accountModel = MarginAccount(minimumEquity = 50_000.0))

        val feed = RandomWalk.lastYears(5)
        val account = run(feed,strategy, broker = broker)
        println(account)
    }

    @Suppress("CyclomaticComplexMethod")
    @Test
    @Ignore
    internal fun tenkan() {

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

        val strategy = TaLibSignalStrategy { asset, series ->
            val tenkan = tenkan(series)
            val kijun = kijun(series)
            when {
                tenkan > kijun && series.open.last() < tenkan && series.close.last() > tenkan -> Signal.buy(
                    asset
                )

                tenkan < kijun && series.close.last() < kijun -> Signal.sell(asset)
                else -> null
            }
        }


        val feed = RandomWalk.lastYears(5)

        println(feed.timeframe)
        val account = run(feed, strategy)
        println(account)

    }

}


