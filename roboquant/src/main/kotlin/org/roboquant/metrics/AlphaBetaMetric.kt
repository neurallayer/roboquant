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

package org.roboquant.metrics


import org.hipparchus.stat.correlation.Covariance
import org.hipparchus.stat.descriptive.moment.Variance
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.returns
import org.roboquant.common.totalReturn
import org.roboquant.feeds.Event
import org.roboquant.common.PriceSerie

/**
 * Calculates the Alpha and Beta of an account. This implementation not only looks at the open positions, but
 * looks at the returns of the complete account, so including cash balances.
 *
 * - Alpha is a measure of the performance of an investment as compared to the market
 * - Beta is a measure of the volatility (or systematic risk) of the account compared to the market
 *
 * The provided risk-free return should be for the same duration as period.
 *
 * @property referenceAsset Which asset to use as reference for the market volatility and returns, for example S&P 500
 * @property period Over how many events to calculate the beta
 * @property priceType The type of price to use, default is "DEFAULT"
 * @property riskFreeReturn the risk-free return, 1% is 0.01. Default is 0.0
 * @property onlyAfterInitialTrade should we only start measuring after an initial trade has been executed, default is
 * false
 * @constructor
 */
class AlphaBetaMetric(
    private val referenceAsset: Asset,
    private val period: Int,
    private val priceType: String = "DEFAULT",
    private val riskFreeReturn: Double = 0.0,
    private val onlyAfterInitialTrade: Boolean = false
) : Metric {

    private val marketData = PriceSerie(period + 1)
    private val portfolioData = PriceSerie(period + 1)

    /**
     * Based on the provided [account] and [event], calculate any metrics and return them.
     */
    override fun calculate(account: Account, event: Event): MetricResults {
        val action = event.prices[referenceAsset]

        // Can we already start recording measures or do we have to wait for
        // an initial trade
        val start = !onlyAfterInitialTrade || account.trades.isNotEmpty()

        if (action !== null && start) {
            val price = action.getPrice(priceType)
            marketData.add(price)

            val equity = account.equity
            val value = account.convert(equity, time = event.time).value
            portfolioData.add(value)

            if (marketData.isFull() && portfolioData.isFull()) {
                val x1 = marketData.toDoubleArray()
                val x2 = portfolioData.toDoubleArray()

                val marketReturns = x1.returns()
                val portfolioReturns = x1.returns()

                val covariance = Covariance().covariance(portfolioReturns, marketReturns)
                val variance = Variance().evaluate(marketReturns)
                val beta = covariance / variance

                val alpha =
                    (x1.totalReturn() - riskFreeReturn) - beta * (x2.totalReturn() - riskFreeReturn)
                return mapOf(
                    "account.alpha" to alpha,
                    "account.beta" to beta
                )
            }
        }
        return emptyMap()
    }

    override fun reset() {
        portfolioData.clear()
        marketData.clear()
    }
}
