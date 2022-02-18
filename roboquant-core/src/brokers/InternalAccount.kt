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

package org.roboquant.brokers

import org.roboquant.common.Amount
import org.roboquant.common.Config
import org.roboquant.common.Config.baseCurrency
import org.roboquant.common.Currency
import org.roboquant.common.Wallet
import org.roboquant.orders.Order
import java.time.Instant
import java.util.*


/**
 * Internal Account is only used by broker implementations, like the SimBroker. The broker is the only one with a
 * referene to this account and will commnuicate to the outside world (Policy and Metrics) only the immutable [Account]
 * version.
 *
 * - Cash balances in the account
 * - the [portfolio] with its assets
 * - The past [trades]
 * - The [orders], both open (still in progress) and already closed ones
 *
 * It also supports multi-currency trading through the use of a pluggable currency converter. Without configuring such
 * plug-in it still supports single currency trading, so when all assets and cash balances are denoted in a single
 * currency.
 *
 * Only a broker should ever update the account. For other components like a policy, it should be used read-only.
 *
 * @property baseCurrency The base currency to use for things like reporting
 * @constructor Create a new Account
 */
class InternalAccount(
    var baseCurrency: Currency = Config.baseCurrency
) {

    /**
     * When was the account last updated
     */
    var lastUpdate: Instant = Instant.MIN

    /**
     * The trades that has been executed
     */
    val trades = LinkedList<Trade>()

    /**
     * All orders
     */
    val orders = LinkedList<Order>()

    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    val cash: Wallet = Wallet()

    /**
     * Total cash balance hold in this account denoted in the [baseCurrency] of the account.
     */
    val cashAmount: Amount
        get() = convert(cash)

    /**
     * Remaining buying power of the account denoted in the [baseCurrency] of the account.
     */
    var buyingPower: Amount = Amount(baseCurrency, 0.0)

    /**
     * Portfolio with its positions
     */
    val portfolio: Portfolio = Portfolio()

    /**
     * Total equity value
     */
    val equity: Wallet
        get() = cash + portfolio.value

    /**
     * Clear all the state in this account.
     */
    internal fun clear() {
        lastUpdate = Instant.MIN
        trades.clear()
        orders.clear()
        portfolio.clear()
        cash.clear()
    }


    fun convert(w: Wallet) = w.convert(toCurrency = baseCurrency, time = lastUpdate)
    fun convert(a: Amount) = a.convert(to = baseCurrency, time = lastUpdate)

    /**
     * Create an immutable [Account] instance that can be shared with other components (Policy and Metric).
     */
    fun toAccount(): Account {
        return Account(
            baseCurrency,
            lastUpdate,
            cash.clone(),
            trades.toList(),
            orders.toList(),
            portfolio.positions.toList(),
            buyingPower
        )
    }

}


