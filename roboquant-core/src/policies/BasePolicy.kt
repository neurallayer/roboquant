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

@file:Suppress("unused")

package org.roboquant.policies

import org.roboquant.brokers.Account
import org.roboquant.common.Amount
import org.roboquant.common.Asset
import org.roboquant.common.Cash
import org.roboquant.common.Currency
import org.roboquant.metrics.MetricResults


/**
 * Contains a number of utility methods that are useful when implementing a new policy. For example
 * how to deal with conflicting signals or how to handle amounts in a multi-currency environment. It also contains
 * a simple method to record metrics.
 *
 * @constructor Create empty Base policy
 */
abstract class BasePolicy(private val prefix: String = "policy.", var recording: Boolean = false) : Policy {

    private val metrics = mutableMapOf<String, Number>()

    /**
     * Record a metric
     *
     * @param key The name of the metric
     * @param value The value of the metric
     */
    protected fun record(key: String, value: Number) {
        if (!recording) return
        metrics["$prefix$key"] = value
    }

    override fun getMetrics(): MetricResults {
        val result = metrics.toMap()
        metrics.clear()
        return result
    }


    override fun reset() {
        metrics.clear()
    }

    /**
     * Convert an amount in account currency to asset currency
     *
     * @param amount
     * @param assetCurrency
     * @param account
     * @return
     */
    open fun convertToAssetCurrency(amount: Double, assetCurrency: Currency, account: Account): Double {
        return if (assetCurrency === account.baseCurrency) {
            amount
        } else {
            val w = Cash(Amount(account.baseCurrency, amount))
            account.convert(w, assetCurrency).value
        }

    }

    /**
     * Calculate the volume (= number of assets) that can be bought with the provided
     * buying power. The buying power is in the same baseCurrency fo the account and the price
     * is in the currency of that asset.
     *
     * @param buyingPower
     * @param asset
     * @param price
     * @param account
     * @return
     */
    protected fun calcVolume(buyingPower: Double, asset: Asset, price: Double, account: Account): Double {
        val singleContractCost = asset.multiplier * price
        val availableAssetCash = account.convert(Amount(account.baseCurrency, buyingPower), asset.currency)
        return availableAssetCash.value / singleContractCost
    }


    /**
     * Get the potential open positions if the open orders would be processed. Open orders that reduce position size
     * are excluded from this calculation. So this is a "worst" kind of scenario from an exposure point of view.
     *

    fun getPotentialPositions(account: Account): Map<Asset, Double> {
    val result = mutableMapOf<Asset, Double>()
    val op = account.portfolio.openPositions.map { it.key to it.value.quantity }
    result.putAll(op)
    account.openOrders.forEach {
    val pos = result.getOrDefault(it.asset, 0.0)
    val newPos = pos + it.remaining

    // Only in case of an increase of the exposed position we include an open order
    if (newPos.absoluteValue > pos.absoluteValue)
    result.get(it.asset) = newPos
    }
    return result
    }
     */

}