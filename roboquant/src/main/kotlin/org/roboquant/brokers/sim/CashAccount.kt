package org.roboquant.brokers.sim

import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.sumOf

/**
 * Basic calculator that calculates: cash balance - open orders. So no leverage or margin is available for trading.
 * This is the default BuyingPower and can be used to model a plain Cash Account.
 *
 * You should not short positions when using the CashModel since that is almost never allowed in the real world
 * and also not supported. However if you do, the short exposure is deducted from the cash amount.
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