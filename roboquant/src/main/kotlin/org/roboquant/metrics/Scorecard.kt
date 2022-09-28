package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.brokers.realizedPNL
import org.roboquant.brokers.unrealizedPNL
import org.roboquant.common.sumOf
import org.roboquant.feeds.Event


/**
 * Scorecard calculates a common set of metrics that can be used to evaluate the performance of a strategy. If you
 * don't know what particular metrics to monitor, this is a good starting point.
 */
class Scorecard : Metric {

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

        return metricResultsOf(
            "winners" to winners,
            "winning.total" to totalWinning,
            "winning.avg" to totalWinning / winners,

            "loosers" to loosers,
            "loosers.total" to totalLoosing,
            "loosing.avg" to totalLoosing/loosers,

            "profit.realized" to realizedPNL,
            "profit.unrealized" to unrealizedPNL,
            "equity" to equity,

            "mdd" to maxMDD
        )
    }

    override fun reset() {
        maxEquity = Double.NaN
        minEquity = Double.NaN
        maxMDD = Double.NaN
    }
}