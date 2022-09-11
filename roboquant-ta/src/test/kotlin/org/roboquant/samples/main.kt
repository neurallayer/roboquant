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
import org.roboquant.metrics.AccountSummary
import org.roboquant.metrics.MetricResults
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Order
import org.roboquant.policies.DefaultPolicy
import org.roboquant.strategies.EMACrossover
import org.roboquant.strategies.Signal
import org.roboquant.ta.TaLibMetric
import org.roboquant.ta.TaLibStrategy

fun vwap() {
    val strategy = TaLibStrategy.vwap(20, 100)
    val feed = AvroFeed.sp500()

    val rq = Roboquant(strategy)
    rq.run(feed)

    println(rq.broker.account.summary())
}


fun customPolicy() {

    /**
     * Custom Policy that captures the ATR (Average True Range) and uses it to set limit orders
     */
    class MyPolicy(private val atrPercentage: Double = 0.02, private val atrPeriod: Int = 5) : DefaultPolicy() {

        // use atr metric
        private val atr = TaLibMetric("atr", atrPeriod) {
            atr(it, atrPeriod)
        }
        private var atrMetrics: MetricResults = emptyMap()

        override fun act(signals: List<Signal>, account: Account, event: Event): List<Order> {
            atrMetrics = atr.calculate(account, event)
            return super.act(signals, account, event)
        }

        /**
         * Create limit BUY and SELL orders with the limit based on the ATR of the asset
         */
        override fun createOrder(signal: Signal, size: Size, price: Double): Order? {
            val value = atrMetrics["atr.${signal.asset.symbol.lowercase()}"]
            if (value != null) {
                val direction = if (size > 0) 1 else -1
                val limit = price - direction * value * atrPercentage
                // println("$direction $price $limit")
                return LimitOrder(signal.asset, size, limit)
            }
            return null
        }

        override fun reset() {
            atr.reset()
            super.reset()
        }

    }

    val roboquant = Roboquant(EMACrossover.EMA_12_26, AccountSummary(), policy = MyPolicy(atrPeriod = 12))
    val feed = AvroFeed.sp500()
    roboquant.run(feed)
    roboquant.broker.account.summary().print()

}


fun main() {
    when ("VWAP") {
        "VWAP" -> vwap()
        "POLICY" -> customPolicy()
    }
}