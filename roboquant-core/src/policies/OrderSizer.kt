/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import java.lang.Double.min
import java.math.BigDecimal
import kotlin.math.floor

/**
 * Sizer helps a policy to determine the sizing of orders.
 *
 * @constructor Create empty Sizer
 */
interface OrderSizer {

    /**
     * Determine the maximum order size for the provided asset.
     */
    fun size(asset: Asset, account: Account, remaining: Double, price: Double): Double
}

/**
 * Define a maximum percentage of total equity value you want to limit your orders to, assuming there is sufficient
 * bying power. Additional you can define a absolute minimum amount (denoted in the baseCurrency of the account).
 *
 * This implemntation also allows for fractional order sizes. A fraction of 50 for example allows orders to be a
 * multiple of 1/50
 *
 * @property maxPercentage Maximum percentage of equity to allocate to a single order, default is 1% (0.01)
 * @property minAmount mininum absolute amount of any order, default is 0.0
 * @property fractions fractions to allow for order size, default is 1 (no fractions)
 *
 */
class PercentageOrderSizer(
    private val maxPercentage: Double = 0.01,
    private val minAmount: Double = 0.0,
    fractions: Int = 1
) : OrderSizer {

    private val fractions = BigDecimal.valueOf(fractions.toLong())

    private fun rounding(asset: Asset, amount: Double, price: Double): Double {
        val singleContractValue = asset.value(1.0, price)
        val volume = floor(amount * fractions.toInt() / singleContractValue)
        val result = BigDecimal.valueOf(volume).setScale(20) / fractions
        return result.toDouble()
    }

    override fun size(asset: Asset, account: Account, remaining: Double, price: Double): Double {

        // The available amount to spent
        val amountValue = min(account.equityAmount.value * maxPercentage, remaining)
        if (amountValue < minAmount) return 0.0

        // Price denoted in currency of account
        val corrPrice = Amount(asset.currency, price).convert(account.baseCurrency, account.lastUpdate).value
        val volume = rounding(asset, amountValue, corrPrice)

        // Check if based on the calculated volume the total order value would still above the minimum amount.
        val totalAmount = asset.value(volume, corrPrice)
        if (totalAmount < minAmount) return 0.0

        return volume
    }

}
