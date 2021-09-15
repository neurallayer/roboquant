package org.roboquant.metrics

import org.roboquant.Phase
import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.feeds.Event
import org.roboquant.strategies.ta.TALib
import org.roboquant.strategies.utils.MovingWindow

/**
 * Beta is a measure of the volatility (or systematic risk) of the account compared to the market as a whole. This
 * implementation doesn't look only at assets, but looks at the volatility of the whole account so including the
 * cash positions.
 *
 * @property referenceAsset Which asset to use for the market volatility, for example S&P 500
 * @property period Over how many events to calculate the beta
 * @constructor
 *
 */
class AccountBeta(private val referenceAsset: Asset, val period: Int)  : SimpleMetric() {

    private val assetData = MovingWindow(period + 1)
    private val accountData = MovingWindow(period + 1)


    /**
     * Based on the provided account and event, calculate any metrics and return them.
     *
     * @param account
     * @param event
     * @return
     */
    override fun calc(account: Account, event: Event): MetricResults {
        val action = event.prices[referenceAsset]

        if (action !== null) {
            val price = action.getPrice()
            val value = account.convertToCurrency(account.getValue(), now = event.now)
            assetData.add(price)
            accountData.add(value)

            if (assetData.isAvailable() && accountData.isAvailable()) {
                val beta = TALib.beta(assetData.toDoubleArray(), accountData.toDoubleArray(), period)
                return mapOf("account.beta" to beta)
            }
        }
        return mapOf()
    }

    override fun start(phase: Phase) {
        accountData.clear()
        assetData.clear()
    }
}