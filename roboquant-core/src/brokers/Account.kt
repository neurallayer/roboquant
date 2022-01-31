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

@file:Suppress("unused", "unused")

package org.roboquant.brokers

import org.roboquant.common.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.roboquant.common.Config.baseCurrency


/**
 * Account represents a unified brokerage trading account and holds the following state:
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
 * @property exchangeRates Optional a currency convertor to support multi-currency trading
 * @constructor Create a new Account
 */
class Account(
    var baseCurrency: Currency = Config.baseCurrency,
    private val exchangeRates: ExchangeRates? = null,
) : Cloneable, Summarizable {

    /**
     * When was the account last updated
     */
    var lastUpdate: Instant = Instant.MIN

    /**
     * The trades that has been executed
     */
    val trades = Trades()


    /**
     * All orders
     */
    val orders = Orders()

    /**
     * Total cash balance hold in this account. This can be a single currency or multiple currencies.
     */
    val cash : Wallet = Wallet()

    /**
     * Total cash balance hold in this account denoted in the [baseCurrency] of the account.
     */
    val cashAmount : Amount
        get() = convert(cash)

    /**
     * Remaining buying power of the account denoted in the [baseCurrency] of the account.
     */
    var buyingPower : Amount = Amount(baseCurrency, 0.0)

    /**
     * Portfolio with its positions
     */
    val portfolio : Portfolio = Portfolio()

    /**
     * Total equity value
     */
    val equity: Wallet
        get() = cash + portfolio.value


    /**
     * Total equity value denoted in the [baseCurrency] of the account.
     */
    val equityAmount: Amount
        get() = convert(equity)


    /**
     * Clear all the state in the account.
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
     * Provide a short summary that contains the high level account information, the available cash balances and
     * the open positions in the portfolio.
     *
     * @return The summary
     */
    override fun summary(singleCurrency: Boolean): Summary {

        fun c(w:Wallet) :Any = if (singleCurrency) convert(w) else w

        val s = Summary("Account")
        s.add("last update", lastUpdate.truncatedTo(ChronoUnit.SECONDS))
        s.add("base currency", baseCurrency.displayName)
        s.add("cash", c(cash))
        s.add("buying power", buyingPower)
        s.add("equity", c(equity))
        s.add("portfolio", c(portfolio.value))
        s.add("long value", c(portfolio.longValue))
        s.add("short value", c(portfolio.shortValue))
        s.add("realized p&l", c(trades.realizedPnL()))
        s.add("unrealized p&l", c(portfolio.unrealizedPNL()))
        return s
    }

    /**
     * Provide a full summary of the account that contains all cahs, orders, trades and open positions. During back
     * testing this can become a long list of items, so look at [summary] for a shorter summary.
     */
    fun fullSummary(): Summary {
        val s = summary()
        s.add(cash.summary())
        s.add(portfolio.summary())
        s.add(orders.summary())
        s.add(trades.summary())
        return s
    }


    /**
     * Create a snapshot of the account that is guaranteed not to change. It is optimised in that it only
     * creates deep copies for objects that are mutated during a run. Other objects are just copied by
     * reference.
     *
     * @return a new snapshot of the account
     */
    public override fun clone(): Account {
        val account = Account(baseCurrency, exchangeRates)
        account.lastUpdate = lastUpdate
        account.trades.addAll(trades) // Trade is immutable
        account.orders.addAll(orders.map { it.clone() }) // order is not immutable
        account.cash.clear()
        account.cash.deposit(cash)
        account.portfolio.put(portfolio)
        return account
    }


}

