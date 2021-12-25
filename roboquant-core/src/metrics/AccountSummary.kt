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

package org.roboquant.metrics

import org.roboquant.RunPhase
import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * Capture the following statistics about the [Account]:
 *
 * - account.orders  Total number of orders
 * - account.orders.open Total number of open orders
 * - account.trades Total number of trades
 * - account.portfolio.assets Total number of assets in the portfolio
 * - account.cash.currencies Number of currencies hold
 * - account.cash.total Total cash value
 * - account.buyingPower Buying power available in the account
 * - account.value Total value of the account
 * - account.change Change of value of the account
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
            // "account.orders.open" to account.orders.open.size,
            "account.trades" to account.trades.size,
            "account.portfolio.assets" to account.portfolio.positions.size,
            "account.cash.currencies" to account.total.currencies.size,
            "account.cash.total" to account.getTotalCash(now = event.now),
            "account.buyingPower" to account.buyingPower
        )

        if (includeValue) {
            val value = account.convertToCurrency(account.getMarketValue(), now = event.now)
            result["account.value"] = value
            result["account.change"] = if (lastValue == null) 0.0 else value - lastValue!!
            lastValue = value
        }

        return result
    }

    override fun start(runPhase: RunPhase) {
        lastValue = null
    }
}