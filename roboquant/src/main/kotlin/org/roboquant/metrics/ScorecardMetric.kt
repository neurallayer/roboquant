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
import org.roboquant.brokers.realizedPNL
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.common.sumOf
import org.roboquant.feeds.Event


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

    private var maxEquity = Double.NaN
    private var minEquity = Double.NaN
    private var maxMDD = Double.NaN

    private fun updateMDD(equity: Double) {
        if (maxEquity.isNaN() || equity > maxEquity) {
            maxEquity = equity
            minEquity = equity
        } else if (equity < minEquity) minEquity = equity


        val mdd = (minEquity - maxEquity) / maxEquity

        if (maxMDD.isNaN() || mdd< maxMDD) maxMDD = mdd
    }


    override fun calculate(account: Account, event: Event): MetricResults {

        val winnersList = account.trades.filter { it.pnl > 0 }.toList()
        val winners = winnersList.count()
        val totalWinning = winnersList.sumOf { it.pnl }.convert(account.baseCurrency, event.time).value

        val loosersList = account.trades.filter { it.pnl < 0.0 }
        val loosers = loosersList.count()
        val totalLoosing = loosersList.sumOf { it.pnl }.convert(account.baseCurrency, event.time).value

        val realizedPNL = account.trades.realizedPNL.convert(account.baseCurrency, event.time).value
        val unrealizedPNL = account.positions.unrealizedPNL.convert(account.baseCurrency, event.time).value
        val equity = account.equity.convert(account.baseCurrency, event.time).value

        updateMDD(equity)

        val prefix ="scorecard."

        return metricResultsOf(
            "${prefix}winners" to winners,
            "${prefix}winning.total" to totalWinning,
            "${prefix}winning.avg" to totalWinning / winners,

            "${prefix}losers" to loosers,
            "${prefix}losers.total" to totalLoosing,
            "${prefix}losing.avg" to totalLoosing/loosers,

            "${prefix}profit.realized" to realizedPNL,
            "${prefix}profit.unrealized" to unrealizedPNL,
            "${prefix}equity" to equity,

            "${prefix}mdd" to maxMDD
        )
    }

    override fun reset() {
        maxEquity = Double.NaN
        minEquity = Double.NaN
        maxMDD = Double.NaN
    }
}