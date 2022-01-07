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

import org.roboquant.common.Amount
import org.roboquant.common.Wallet
import org.roboquant.common.Currency
import org.roboquant.common.Summary
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.naming.ConfigurationException


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
 * @property currencyConverter Optional a currency convertor to support multi-currency trading
 * @constructor Create a new Account
 */
class Account(
    var baseCurrency: Currency = Currency.USD,
    private val currencyConverter: CurrencyConverter? = null,
) : Cloneable {

    /**
     * When was the account last updated
     */
    var time: Instant = Instant.MIN

    /**
     * The trades that has been executed
     */
    val trades = Trades()


    /**
     * All orders
     */
    val orders = Orders()

    /**
     * Total cash balance hold in this account. This is the account cash deposits plus all realized PNL so far.
     * PLase note that not all this cash is available.
     */
    val cash : Wallet = Wallet()


    val cashAmount : Amount
        get() = convert(cash)


    var buyingPower : Amount = Amount(baseCurrency, Double.NaN)
        get() = if (field.value.isNaN()) cashAmount else field


    /**
     * Portfolio with its positions
     */
    val portfolio : Portfolio = Portfolio()


    /**
     * Equity
     */
    val equity
        get() = cash + portfolio.value


    val equityAmount
        get() = convert(equity)


    /**
     * Clear all the state in the account.
     */
    internal fun clear() {
        time = Instant.MIN
        trades.clear()
        orders.clear()
        portfolio.clear()
        cash.clear()
    }


    /**
     * Get the total cash balance hold in this account converted to a single currency amount.
     *
     * @param currency The currency to convert to, default is the account base currency
     * @param now The time to use for the conversion, default is the account last update time
     * @return The total amount
     */
    fun getCashAmount(currency: Currency = baseCurrency, now: Instant = time): Amount {
        return convert(cash, currency, now)
    }




    /**
     * Provide a short summary that contains the high level account information, the available cash balances and
     * the open positions in the portfolio.
     *
     * @return The summary
     */
    fun summary(): Summary {
        val s = Summary("Account")
        s.add("last update", time.truncatedTo(ChronoUnit.SECONDS))
        s.add("base currency", baseCurrency.displayName)
        s.add("equity", equityAmount)
        s.add("buying power", buyingPower)
        s.add(cash.summary())

        val tradesSummary = Summary("Trades")
        tradesSummary.add("total", trades.size)
        val realizedPNL = convert(trades.realizedPnL())
        tradesSummary.add("realized p&l", realizedPNL.formatValue())
        tradesSummary.add("assets", trades.assets.size)
        val fee = convert(trades.totalFee())
        tradesSummary.add("fee", fee.formatValue())
        s.add(tradesSummary)

        val orderSummary = Summary("Orders")
        orderSummary.add("total", orders.size)
        orderSummary.add("open", orders.open.size)
        orderSummary.add("closed", orders.closed.size)
        s.add(orderSummary)

        val portfolioSummary = Summary("Portfolio")
        portfolioSummary.add("open positions", portfolio.positions.size)
        portfolioSummary.add("long positions", portfolio.longPositions.size)
        portfolioSummary.add("short positions", portfolio.shortPositions.size)
        val unrealizedValue = convert(portfolio.value)
        portfolioSummary.add("estimated value", unrealizedValue.formatValue())
        val unrealizedPNL = convert(portfolio.unrealizedPNL())
        portfolioSummary.add("unrealized p&l", unrealizedPNL.formatValue())
        s.add(portfolioSummary)

        return s
    }

    /**
     * Provide a full summary of the account that contains all cahs, orders, trades and open positions. During back
     * testing this can become a long list of items, so look at [summary] for a shorter summary.
     */
    fun fullSummary(): Summary {
        val s = Summary("Account")
        s.add("last update", time.truncatedTo(ChronoUnit.SECONDS))
        s.add("base currency", baseCurrency.displayName)
        s.add("buying power", buyingPower)
        s.add(cash.summary())
        s.add(portfolio.summary())
        s.add(orders.summary())
        s.add(trades.summary())
        return s
    }

    /**
     * Convert a [Wallet] value into a single currency amount. If no currencyConverter has been configured and this method
     * is invoked when a conversion is required, it will throw a [ConfigurationException].
     *
     * @param wallet The cash values to convert from
     * @param toCurrency The currency to convert the cash to, default is the baseCurrency of the account
     * @param now The time to use for the exchange rate, default is the last update time of the account
     * @return The converted amount as a Double
     */
    fun convert(wallet: Wallet, toCurrency: Currency = baseCurrency, now: Instant = time): Amount {
        var sum = 0.0
        for (amount in wallet.toAmounts()) {
            sum += if (amount.currency === toCurrency) {
                amount.value
            } else {
                currencyConverter?.convert(amount, toCurrency, now)?.value
                    ?: throw ConfigurationException("No currency converter defined to convert  $amount to $toCurrency")
            }
        }
        return Amount(toCurrency, sum)
    }



    /**
     * Convert an Amount into a single currency amount. If no currencyConverter has been configured and this method is
     * called and a conversion is required, it will throw a [ConfigurationException].
     *
     */
    fun convert(amount: Amount, toCurrency: Currency = baseCurrency, now: Instant = time): Amount {
        return if (amount.currency === toCurrency || amount.value == 0.0) {
            Amount(toCurrency, amount.value)
        } else {
            currencyConverter?.convert(amount, toCurrency, now)
                ?: throw ConfigurationException("No currency converter defined to convert $amount to $toCurrency")

        }
    }


    /**
     * Create a snapshot of the account that is guaranteed not to change. It is optimised in that it only
     * creates deep copies for objects that are mutated during a run. Other objects are just copied by
     * reference.
     *
     * @return a new snapshot of the account
     */
    public override fun clone(): Account {
        val account = Account(baseCurrency, currencyConverter)
        account.time = time
        account.trades.addAll(trades) // Trade is immutable
        account.orders.addAll(orders.map { it.clone() }) // order is not immutable
        account.cash.clear()
        account.cash.deposit(cash)
        account.portfolio.put(portfolio)
        return account
    }


}

