/*
 * Copyright 2020-2023 Neural Layer
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
 * Capture the following high-level statistics about the [Account]:
 *
 * - `account.order`  Total number of orders,
 * - `account.trades`  Total number of trades,
 * - `account.positions` Total number of open positions
 * - `account.cash` Total cash value
 * - `account.buyingpower` Buying power available
 * - `account.equity` the equity value of the account (= cash + positions)
 *
 * All monetary values are denoted in the base currency of the account
 *
 * @constructor Create new AccountMetric instance
 */
class AccountMetric : Metric {

    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event) = buildMap {
        put("account.orders", (account.openOrders.size + account.closedOrders.size).toDouble())
        put("account.trades", account.trades.size.toDouble())
        put("account.positions", account.positions.size.toDouble())
        put("account.cash", account.convert(account.cash, event.time).value)
        put("account.buyingpower", account.buyingPower.value)
        put("account.equity", account.convert(account.equity, event.time).value)
    }


}
