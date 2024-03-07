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

package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.feeds.Event
import java.time.Instant

/**
 * Scorecard calculates a common set of metrics that can be used to evaluate the performance of a strategy. If you
 * don't know which particular metrics to monitor, this is a good starting point.
 *
 * - The winners
 * - The losers
 * - The profit
 * - The max draw-down
 *
 */
class ScorecardMetric : Metric {

    private var peakValue = Double.NaN
    private var mdd = Double.NaN
    private var firstTime = Instant.MIN

    private fun updateMDD(equity: Double) {
        if (peakValue.isNaN() || equity > peakValue) peakValue = equity
        val dd = (equity - peakValue) / peakValue
        if (mdd.isNaN() || dd < mdd) mdd = dd
    }

    override fun calculate(account: Account, event: Event): Map<String, Double> {

        val pnl = account.trades.map { it.pnl.convert(account.baseCurrency, event.time).value }

        val winnerList = pnl.filter { it > 0.0 }
        val winners = winnerList.size
        val totalWinning = winnerList.sum()
        val maxWinning = winnerList.maxOrNull() ?: Double.NaN

        val loserList = pnl.filter { it < 0.0 }
        val losers = loserList.size
        val totalLoosing = loserList.sum()
        val maxLoosing = loserList.minOrNull() ?: Double.NaN

        val realizedPNL = pnl.sum()

        val unrealizedPNL = account.positions.unrealizedPNL.convert(account.baseCurrency, event.time).value
        val equity = account.equity.convert(account.baseCurrency, event.time).value

        val cash = account.cash.convert(account.baseCurrency, event.time).value


        updateMDD(equity)

        val prefix = "scorecard."

        return metricResultsOf(
            "${prefix}trades" to account.trades.size,
            "${prefix}orders.closed" to account.closedOrders.size,
            "${prefix}orders.open" to account.openOrders.size,
            "${prefix}positions" to account.positions.size,
            "${prefix}buyingpower" to account.buyingPower.value,
            "${prefix}cash" to cash,

            "${prefix}winners" to winners,
            "${prefix}winners.total" to totalWinning,
            "${prefix}winners.avg" to totalWinning / winners,
            "${prefix}winners.largest" to maxWinning,

            "${prefix}losers" to losers,
            "${prefix}losers.total" to totalLoosing,
            "${prefix}losers.avg" to totalLoosing / losers,
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
    }
}
