package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event


/**
 * Captures metrics for all open positions within the portfolio, so you can see how these progresses over the
 * duration of the run. Per asset, it will record quantity, value, cost and unrealized P&L for that position.
 *
 */
class OpenPositions : SimpleMetric() {

    override fun calc(account: Account, event: Event): MetricResults {
        val result = mutableMapOf<String, Number>()
        val positions = account.portfolio.positions.values
        for (position in positions) {
            val asset = position.asset
            val name = "position.${asset.symbol}"
            result["$name.quantity"] = position.quantity
            result["$name.value"] = position.value
            result["$name.cost"] = position.totalCost
            result["$name.pnl"] = position.pnl
        }
        return result
    }
}