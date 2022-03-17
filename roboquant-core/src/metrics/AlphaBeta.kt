/*
 * Copyright 2021 Neural Layer
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

import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.totalReturns
import org.roboquant.feeds.Event
import org.roboquant.strategies.ta.TALib
import org.roboquant.strategies.utils.MovingWindow

/**
 * Calculates the Alpha and Beta of a portfio. This implementation not only looka at the assets in the portfolio, but
 * looks at the returns of the whole account, so including the cash balances.
 *
 * - Alpha is a measure of the performance of an investment as compared to the market as a whole.
 * - Beta is a measure of the volatility (or systematic risk) of the account compared to the market as a whole.
 *
 * @property referenceAsset Which asset to use for the market volatility, for example S&P 500
 * @property period Over how many events to calculate the beta
 * @constructor
 *
 */
class AlphaBeta(
    private val referenceAsset: Asset,
    private val period: Int,
    private val priceType: String = "DEFAULT",
    private val riskFreeReturn : Double = 0.0,
    private val onlyAfterInitialTrade: Boolean = false
) : SimpleMetric() {

    private val marketData = MovingWindow(period + 1)
    private val portfolioData = MovingWindow(period + 1)



    /**
     * Based on the provided account and event, calculate any metrics and return them.
     *
     * @param account
     * @param event
     * @return
     */
    override fun calc(account: Account, event: Event): MetricResults {
        val action = event.prices[referenceAsset]

        // Can we already start recording measures or do we have to wait for
        // an initial trade
        val start = ! onlyAfterInitialTrade || account.trades.isNotEmpty()

        if (action !== null && start) {
            val price = action.getPrice(priceType)
            marketData.add(price)

            val value = account.equity.convert(time = event.time).value
            portfolioData.add(value)

            if (marketData.isAvailable() && portfolioData.isAvailable()) {
                val x1 = marketData.toDoubleArray()
                val x2 = portfolioData.toDoubleArray()
                val beta = TALib.beta(x1, x2, period)
                val alpha = (x1.totalReturns() - riskFreeReturn) - beta * (x2.totalReturns() - riskFreeReturn)
                return mapOf(
                    "account.alpha" to alpha,
                    "account.beta" to beta
                )
            }
        }
        return mapOf()
    }

    override fun start(runPhase: RunPhase) {
        portfolioData.clear()
        marketData.clear()
    }
}
