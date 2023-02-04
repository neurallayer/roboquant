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

import org.roboquant.brokers.Account
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.common.Asset
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event
import java.time.Instant


class AssetReturn(private val startTime: Instant, private val startPrice: Double) {

    private var lastTime: Instant = startTime
    private var lastPrice: Double = startPrice

    private val duration
        get() = Timeframe(startTime, lastTime).duration

    fun update(time: Instant, price: Double) {
        lastTime = time
        lastPrice = price
    }

    val returns : Double
        get() = (lastPrice - startPrice) / startPrice

    fun weight(timeframe: Timeframe): Double {
        return 1.0 * duration.toMillis() / timeframe.duration.toMillis()
    }


}

/**
 * Scorecard calculates a common set of metrics that can be used to evaluate the performance of a strategy. If you
 * don't know which particular metrics to monitor, this is a good starting point.
 *
 * - winners
 * - losers
 * - profit
 * - max draw-down
 *
 */
class ScorecardMetric : Metric {

    private var peakValue = Double.NaN
    private var mdd = Double.NaN
    private var firstTime = Instant.MIN
    private val assetsReturns = mutableMapOf<Asset, AssetReturn>()

    private fun updateMDD(equity: Double) {
        if (peakValue.isNaN() || equity > peakValue) peakValue = equity
        val dd = (equity - peakValue) / peakValue
        if (mdd.isNaN() || dd < mdd) mdd = dd
    }

    private fun updateAlpha(event: Event) : Double {

        for ((asset, action) in event.prices) {
            val price = action.getPrice()
            val assetReturn = assetsReturns.getOrPut(asset) {  AssetReturn(event.time, price) }
            assetReturn.update(event.time, price)
        }

        return if (firstTime == Instant.MIN) {
            firstTime = event.time
            0.0
        } else {
            var sumReturn = 0.0
            var sumW = 0.0
            val tf = Timeframe(firstTime, event.time)
            for (assetReturn in assetsReturns.values) {
                val w = assetReturn.weight(tf)
                sumReturn += assetReturn.returns * w
                sumW += w
            }
            sumReturn / sumW
        }

    }

    override fun calculate(account: Account, event: Event): MetricResults {

        val pnl =  account.trades.map { it.pnl.convert(account.baseCurrency, event.time).value }

        val winnersList = pnl.filter { it > 0.0 }
        val winners = winnersList.count()
        val totalWinning = winnersList.sum()
        val maxWinning = winnersList.maxOrNull() ?: Double.NaN

        val loosersList = pnl.filter { it < 0.0 }
        val loosers = loosersList.count()
        val totalLoosing = loosersList.sum()
        val maxLoosing = loosersList.minOrNull()  ?: Double.NaN

        val realizedPNL = pnl.sum()

        val unrealizedPNL = account.positions.unrealizedPNL.convert(account.baseCurrency, event.time).value
        val equity = account.equity.convert(account.baseCurrency, event.time).value

        val cash = account.cash.convert(account.baseCurrency, event.time).value
        val mktReturn = updateAlpha(event)

        updateMDD(equity)

        val prefix = "scorecard."

        return metricResultsOf(
            "${prefix}trades" to account.trades.size,
            "${prefix}orders.closed" to account.closedOrders.size,
            "${prefix}orders.open" to account.openOrders.size,
            "${prefix}positions" to account.positions.size,
            "${prefix}buyingpower" to account.buyingPower.value,
            "${prefix}cash" to cash,
            "${prefix}marketreturn" to mktReturn,

            "${prefix}winners" to winners,
            "${prefix}winners.total" to totalWinning,
            "${prefix}winners.avg" to totalWinning / winners,
            "${prefix}winners.largest" to maxWinning,

            "${prefix}losers" to loosers,
            "${prefix}losers.total" to totalLoosing,
            "${prefix}losers.avg" to totalLoosing / loosers,
            "${prefix}losers.largest" to maxLoosing,

            "${prefix}profit.realized" to realizedPNL,
            "${prefix}profit.unrealized" to unrealizedPNL,
            "${prefix}equity" to equity,

            "${prefix}mdd" to mdd
        )
    }

    override fun reset() {
        peakValue = Double.NaN
        mdd = Double.NaN
        firstTime = Instant.MIN
        assetsReturns.clear()
    }
}