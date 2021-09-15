package org.roboquant.metrics

import org.roboquant.brokers.Account
import org.roboquant.common.Cash
import org.roboquant.feeds.Event
import kotlin.math.absoluteValue

/**
 * Calculate the portfolio total net and gross exposure relative to the total account value.
 *
 * - Net exposure is the difference between a portfolio long positions and its short positions and is expressed
 *   as a percentage of total account value
 *
 * - Gross exposure refers to the absolute level of a portfolio investments. It takes into account the value of both a
 * portfolio's long positions and short positions and is expressed as a percentage of total account value.
 *
 * @constructor Create empty Portfolio exposure
 */
class PortfolioExposure : SimpleMetric() {
    /**
     * Calculate any metrics given the event of information. This will be called at the
     * end of each step in a run.
     *
     * If no values are calculated, an empty map should be returned.
     *
     * @param account
     * @return
     */
    override fun calc(account: Account, event: Event): MetricResults {
        val longExposure = Cash()
        val shortExposure = Cash()
        val now = event.now

        for ((asset, position) in account.portfolio.positions) {
            if (position.long)
                longExposure.deposit(asset.currency, position.value)
            else
                shortExposure.deposit(asset.currency, position.value)
        }

        val longExposureValue = account.convertToCurrency(longExposure, now = now)
        val shortExposureValue = account.convertToCurrency(shortExposure, now = now)
        val netExposureValue = longExposureValue + shortExposureValue
        val grossExposureValue = longExposureValue.absoluteValue + shortExposureValue.absoluteValue

        val total = account.getTotalCash(now = now) + netExposureValue

        return mapOf(
            "exposure.net" to netExposureValue / total,
            "exposure.gross" to grossExposureValue / total,
            "exposure.long" to longExposureValue,
            "exposure.short" to shortExposureValue,
        )

    }
}