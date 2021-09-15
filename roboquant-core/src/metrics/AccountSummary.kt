package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * Capture the following statistics about the [Account]:
 *
 *
 * - account.orders.open
 * - account.orders.closed
 * - account.trades
 * - account.portfolio.assets
 * - account.cash.currencies
 * - account.cash.total
 * - account.buyingPower
 * - account.value
 * - account.change
 *
 *
 * @property includeValue include the account value and account value change metrics, default is true
 * @constructor Create new Account Summary metric
 */
class AccountSummary(val includeValue: Boolean = true) : SimpleMetric() {

    private var lastValue: Double? = null

    override fun calc(account: Account, event: Event): MetricResults {
        val result = mutableMapOf(
            "account.orders" to account.orders.size,
            "account.orders.open" to account.orders.open.size,
            "account.trades" to account.trades.size,
            "account.portfolio.assets" to account.portfolio.positions.size,
            "account.cash.currencies" to account.cash.currencies.size,
            "account.cash.total" to account.getTotalCash(now = event.now),
            "account.buyingPower" to account.buyingPower
        )

        if (includeValue) {
            val value = account.convertToCurrency(account.getValue(), now = event.now)
            result["account.value"] = value
            result["account.change"] = if (lastValue == null) 0.0 else value - lastValue!!
            lastValue = value
        }

        return result
    }

    override fun start(phase: Phase) {
        lastValue = null
    }
}