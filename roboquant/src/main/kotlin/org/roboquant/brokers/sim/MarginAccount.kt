/*
 * Copyright 2020-2025 Neural Layer
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
package org.roboquant.brokers.sim

import org.roboquant.brokers.exposure
import org.roboquant.brokers.long
import org.roboquant.brokers.marketValue
import org.roboquant.brokers.short
import org.roboquant.common.Amount
import org.roboquant.common.percent

/**
 * An account model that supports trading with margin. The buying power is calculated using the following steps:
 * ```
 *      1. Long value = long positions * maintenance margin long
 *      2. Short value = short positions * maintenance margin short
 *      3. Excess margin = equity - long value - short value - minimum equity
 *      4. Buying power = excess margin * (1 / initial margin)
 *```
 * Note: currently open orders are not taken into consideration when calculating the total buying power.
 *
 * @property initialMargin the initial margin requirements, default to 50% (0.50)
 * @property maintenanceMarginLong the maintenance margin requirement for long positions, defaults to 30% (0.3)
 * @property maintenanceMarginShort the maintenance margin requirement for short positions, defaults to same value as
 * [maintenanceMarginLong]
 * @property minimumEquity the minimum equity requirement, defaults to 0.0 (denoted in Account.baseCurrency)
 */
class MarginAccount(
    private val initialMargin: Double = 50.percent,
    private val maintenanceMarginLong: Double = 30.percent,
    private val maintenanceMarginShort: Double = maintenanceMarginLong,
    private val minimumEquity: Double = 0.0
) : AccountModel {

    /**
     * Create a margin based on a [leverage]. Effectively, all margins values will be set to 1/leverage. Optional can
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

    /**
     * @see [AccountModel.updateAccount]
     */
    override fun updateAccount(account: InternalAccount) {
        val time = account.lastUpdate
        val currency = account.baseCurrency
        val positions = account.positions

        val excessMargin = account.cash + positions.marketValue()
        excessMargin.withdraw(Amount(currency, minimumEquity))

        val longExposure = positions.long.exposure().convert(currency, time) * maintenanceMarginLong
        excessMargin.withdraw(longExposure)

        val shortExposure = positions.short.exposure().convert(currency, time) * maintenanceMarginShort
        excessMargin.withdraw(shortExposure)

        val buyingPower = excessMargin.convert(currency, time) * (1.0 / initialMargin)
        account.buyingPower = buyingPower
    }

}
