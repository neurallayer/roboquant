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

package org.roboquant.brokers.sim

import org.roboquant.brokers.*
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.Amount
import org.roboquant.common.Logging

/**
 * Interface for modelling different types of Accounts used in the [SimBroker], like a [CashAccount] or [MarginAccount]
 *
 * Currently, the main functionality is that at the end of each step the buying power is re-calculated and stored
 * in the attribute [Account.buyingPower]. But in the future the implementation could make other updates to the account,
 * for example calculate borrow feeds or interest rates that might apply.
 */
interface AccountModel {

    /**
     * Update the [account] based on the rules within the account model. Currently only the buying-power is calculated
     * and set.
     */
    fun updateAccount(account: InternalAccount)

}

/**
 * Basic calculator that calculates: cash balance - open orders. So no leverage or margin is available for trading.
 * This is the default BuyingPower and can be used to model a plain Cash Account.
 *
 * You should not short positions when using the CashModel since that is almost never allowed in the real world
 * and also not supported. It will generate warning messages.
 *
 * Note: currently open orders are not taken into consideration when calculating the total buying power
 *
 * @property minimum the minimum amount of cash balance required to maintain in the account, defaults to 0.0
 */
class CashAccount(private val minimum: Double = 0.0) : AccountModel {

    private val logger = Logging.getLogger(CashAccount::class)

    /**
     * @see [AccountModel.updateAccount]
     */
    override fun updateAccount(account: InternalAccount) {
        if (account.portfolio.values.any { it.short }) {
            logger.warn("Having short positions while using cash account is not supported")
        }

        account.buyingPower = account.cash.convert(account.baseCurrency, account.lastUpdate) - minimum
    }

}

/**
 * Account model that supports trading with margin. The buying power is calculated using the following steps:
 *
 *      1. long value = long positions * maintenance margin long
 *      2. short value = short positions * maintenance margin short
 *      3. excess margin = equity - long value - short value - minimum equity
 *      4. buying power = excess margin * ( 1 / initial margin)
 *
 * Note: currently open orders are not taken into consideration when calculating the total buying power.
 *
 * @property initialMargin the initial margin requirements, default to 50% (0.50)
 * @property maintenanceMarginLong the maintenance margin requirement for long positions, defaults to 30% (0.3)
 * @property maintenanceMarginShort the maintenance margin requirement for short positions, defaults to same value as
 * [maintenanceMarginLong]
 * @property minimumEquity the minimum equity requirement, defaults to 0.0 (denoted in [Account.baseCurrency])
 */
class MarginAccount(
    private val initialMargin: Double = 0.50,
    private val maintenanceMarginLong: Double = 0.3,
    private val maintenanceMarginShort: Double = maintenanceMarginLong,
    private val minimumEquity: Double = 0.0
    // private val includeOpenOrders: Boolean = false
) : AccountModel {

    /**
     * Create a margin based on a [leverage]. Effectively all margins values will be set to 1/leverage. Optional can
     * provide a minimum cash amount that needs to remain in the account.
     */
    constructor(leverage: Double, minimum: Double = 0.0) : this(
        1.0 / leverage,
        1.0 / leverage,
        1.0 / leverage,
        minimum
    )

    init {
        require(initialMargin in 0.0..1.0) { "initialMargin between 0.0 and 1.0" }
        require(maintenanceMarginLong in 0.0..1.0) { "maintenanceMarginLong between 0.0 and 1.0" }
        require(maintenanceMarginShort in 0.0..1.0) { "maintenanceMarginShort between 0.0 and 1.0" }
    }

    /*
    override fun getBuyingPower(account: InternalAccount): Amount {
        val excessMargin = account.cash + account.portfolio.marketValue

        val positions = account.portfolio.values
        excessMargin.withdraw(positions.long.exposure * maintenanceMarginLong)
        excessMargin.withdraw(positions.short.exposure * maintenanceMarginShort)
        excessMargin.withdraw(Amount(account.baseCurrency, minimumEquity))
        val buyingPower = excessMargin * (1.0 / initialMargin)
        return buyingPower.convert(account.baseCurrency, account.lastUpdate)
    }
     */

    /**
     * @see [AccountModel.updateAccount]
     */
    override fun updateAccount(account: InternalAccount) {
        val time = account.lastUpdate
        val currency = account.baseCurrency
        val positions = account.portfolio.values

        val excessMargin = account.cash + positions.marketValue
        excessMargin.withdraw(Amount(currency, minimumEquity))

        val longExposure = positions.long.exposure.convert(currency, time) * maintenanceMarginLong
        excessMargin.withdraw(longExposure)

        val shortExposure = positions.short.exposure.convert(currency, time) * maintenanceMarginShort
        excessMargin.withdraw(shortExposure)

        account.buyingPower = excessMargin.convert(currency, time) * (1.0 / initialMargin)
    }

}

