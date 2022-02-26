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

import org.roboquant.brokers.Account
import org.roboquant.feeds.Event

/**
 * Capture the following statistics about the [Account]:
 *
 * - account.orders  Total number of orders
 * - account.orders.open Total number of open orders
 * - account.trades Total number of trades
 * - account.portfolio.assets Total number of assets in the portfolio
 * - account.cash Total cash value
 * - account.buyingPower Buying power available in the account
 * - account.equity Total equity value of the account
 * - account.change Change of value of the account
 *
 * @constructor Create new Account Summary metric
 */
class AccountSummary: SimpleMetric() {


    override fun calc(account: Account, event: Event): MetricResults {
        return mapOf(
            "account.orders" to account.openOrders.size + account.closedOrders.size,
            "account.trades" to account.trades.size,
            "account.positions" to account.portfolio.size,
            "account.cash" to account.cash.convert(time = event.time).value,
            "account.buyingpower" to account.buyingPower.value,
            "account.equity"  to account.equity.convert(time = event.time).value,
        )

    }

}