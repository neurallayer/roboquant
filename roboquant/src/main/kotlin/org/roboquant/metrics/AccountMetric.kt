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

package org.roboquant.metrics

import org.roboquant.common.Account
import org.roboquant.common.Event

/**
 * Capture the following high-level statistics about the [Account]:
 *
 * - `account.order`  Number of orders (within retention period),
 * - `account.trades`  Number of trades (within retention period),
 * - `account.positions` Number of open positions
 * - `account.cash` Total cash value
 * - `account.buyingpower` Buying power available
 * - `account.equity` the equity value of the account (= cash + positions)
 * - `aacount.mdd` the max drawdown of the account
 *
 * All monetary values are denoted in the base currency of the account.
 *
 * @constructor Create new AccountMetric instance
 */
class AccountMetric : Metric {

    private var peak = Double.MIN_VALUE
    private var mdd = Double.MAX_VALUE

    /**
     * @see Metric.calculate
     */
    override fun calculate(event: Event, account: Account) = buildMap {

        val equity = account.convert(account.equity()).value
        if (equity > peak) peak = equity
        val dd = (equity - peak) / peak
        if (dd < mdd) mdd = dd

        put("account.orders", (account.orders.size).toDouble())
        put("account.positions", account.positions.size.toDouble())
        put("account.cash", account.convert(account.cash).value)
        put("account.buyingpower", account.buyingPower.value)
        put("account.equity", equity)
        put("account.mdd", mdd)
    }

    override fun reset() {
        peak = Double.MIN_VALUE
        mdd = Double.MAX_VALUE
    }


}
