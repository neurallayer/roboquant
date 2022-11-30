package org.roboquant.brokers.sim

import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.sumOf

/**
 * Basic calculator that calculates: cash balance - open orders. No leverage or margin is available for trading.
 * This is the default AccountModel and can be used to model a plain trading cash account.
 *
 * You should not short positions when using the CashModel since that is almost never allowed in the real world
 * and also not supported. If you do anyhow, the short exposures are deducted from the buying power. So the used
 * calculation is:
 *
 *      buying power = cash - short exposure
 *
 * Note: currently open orders are not taken into consideration when calculating the total buying power
 *
 * @property minimum the minimum amount of cash balance required to maintain in the account, defaults to 0.0
 */
class CashAccount(private val minimum: Double = 0.0) : AccountModel {

    /**
     * @see [AccountModel.updateAccount]
     */
    override fun updateAccount(account: InternalAccount) {
        val shortExposure = account.portfolio.values.filter { it.short }.sumOf { it.exposure }
        val cash = account.cash - shortExposure

        val buyingPower = cash.convert(account.baseCurrency, account.lastUpdate) - minimum
        account.buyingPower = buyingPower
    }

}