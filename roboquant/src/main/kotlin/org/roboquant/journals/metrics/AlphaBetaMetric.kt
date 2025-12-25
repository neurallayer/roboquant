/*
 * Copyright 2020-2026 Neural Layer
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

package org.roboquant.journals.metrics


import org.hipparchus.stat.correlation.Covariance
import org.hipparchus.stat.descriptive.moment.Variance
import org.roboquant.common.Account
import org.roboquant.common.Asset
import org.roboquant.common.PriceSeries
import org.roboquant.common.Timeframe
import org.roboquant.common.percent
import org.roboquant.common.Event
import org.roboquant.common.Order
import org.roboquant.common.Signal
import java.time.Instant
import java.util.*

/**
 * Calculates the Alpha and Beta of the account.
 *
 * - Alpha measures the performance as compared to the market (universe)
 * - Beta measures the volatility (or systematic risk) compared to the market
 *
 * Market is defined as all the assets that are in the feed. So there is no need to provide a reference asset like the
 * S&P500. This implementation uses the returns of the equity as a measure of the portfolio performance.
 *
 * This can be a CPU intensive metric since it is calculated at each step in a run.
 *
 * @property period Over how many events to calculate the alpha and beta
 * @property priceType The type of price to use for calculating market performance, default is "DEFAULT"
 * @property riskFreeReturn the annualized risk-free return, 1% is 0.01. Default is 0.percent
 * @constructor
 */
class AlphaBetaMetric(
    private val period: Int,
    private val priceType: String = "DEFAULT",
    private val riskFreeReturn: Double = 0.percent,
) : Metric {

    private val marketData = PriceSeries(period)
    private val equityData = PriceSeries(period)
    private var oldPrices = mutableMapOf<Asset, Double>()
    private var initialized = false
    private var oldEquity = 0.0
    private var times = LinkedList<Instant>()

    private fun getMarketReturn(prices: Map<Asset, Double>): Double {
        var sum = 0.0
        var cnt = 0
        for (asset in prices.keys) {
            if (asset in oldPrices) {
                cnt++
                sum += prices.getValue(asset) / oldPrices.getValue(asset) - 1.0
            }
        }
        return sum / cnt
    }

    private val timeframe
        get() = Timeframe(times.first(), times.last(), true)

    /**
     * Calculate total returns over an array of individual returns.
     */
    private fun DoubleArray.product() : Double {
        var result = 1.0
        for (i in indices) result *= get(i) + 1.0
        return result - 1.0
    }

    /**
     * Based on the provided [account] and [event], calculate any metrics and return them.
     */
    override fun calculate(event: Event, account: Account, signals: List<Signal>, orders: List<Order>): Map<String, Double> {
        if (event.prices.isEmpty()) return emptyMap()

        val prices = event.prices.mapValues { it.value.getPrice(priceType) }
        val equity = account.equityAmount().value

        if (initialized) {
            val mr = getMarketReturn(prices)
            marketData.add(mr)
            equityData.add(equity / oldEquity -1)
            times.add(event.time)
            if (times.size > period) times.removeFirst()
            assert(times.size == marketData.size)
        }

        oldPrices.putAll(prices)
        oldEquity = equity
        initialized = true

        if (marketData.isFull() && equityData.isFull()) {
            val mr = marketData.toDoubleArray()
            val pr = equityData.toDoubleArray()
            val beta = Covariance().covariance(mr, pr) / Variance().evaluate(mr)

            val totalMr = timeframe.annualize(mr.product())
            val totalPr = timeframe.annualize(pr.product())
            val alpha = (totalPr - riskFreeReturn) - beta * (totalMr - riskFreeReturn)

            return mapOf(
                "account.alpha" to alpha,
                "account.beta" to beta
            )
        }

        return emptyMap()
    }

    override fun reset() {
        equityData.clear()
        marketData.clear()
        oldPrices.clear()
        oldEquity = 0.0
        times.clear()
        initialized = false
    }
}
