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

import org.roboquant.Roboquant
import org.roboquant.brokers.Account
import org.roboquant.common.Size
import org.roboquant.feeds.Event
import org.roboquant.feeds.avro.AvroFeed
import org.roboquant.metrics.AccountMetric
import org.roboquant.metrics.MetricResults
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Order
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMAStrategy
import org.roboquant.strategies.Signal
import org.roboquant.ta.TaLibMetric


fun customPolicy() {

    /**
     * Custom Policy that extends the DefaultPolicy and captures the ATR (Average True Range) using the TaLibMetric. It
     * then uses the ATR to set the limit amount of a LimitOrder.
     */
    class SmartLimitPolicy(private val atrPercentage: Double = 0.02, private val atrPeriod: Int) : DefaultPolicy() {

        // use TaLibMetric to calculate the ATR values
        private val atr = TaLibMetric("atr", atrPeriod + 1) { atr(it, atrPeriod) }
        private var atrMetrics: MetricResults = emptyMap()

        override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
            // Update the metrics and store the results, so we have them available when the
            // createOrder is invoked.
            atrMetrics = atr.calculate(account, event)

            // Call the regular DefaultPolicy processing
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

    val roboquant = Roboquant(EMAStrategy.EMA_12_26, AccountMetric(), policy = SmartLimitPolicy(atrPeriod = 5))
    val feed = AvroFeed.sp500()
    roboquant.run(feed)
    println(roboquant.broker.account.summary())
}


fun main() {
    when ("POLICY") {
        "POLICY" -> customPolicy()
    }
}