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

import org.roboquant.common.Cash
import org.roboquant.common.Currency
import org.roboquant.common.Summary
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.naming.ConfigurationException


/**
 * Account represents a brokerage trading account and holds the following state:
 *
 * - the total amount of cash in the account
 * - the portfolio with its assets
 * - The past trades
 * - The orders, both open (still in progress) and already closed ones
 * - optionally the buying power available
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
     * Cash balances hold in this account
     */
    val cash = Cash()

    /**
     * Portfolio with its positions
     */
    val portfolio = Portfolio()

    /**
     * How much cash (in base currency denoted) is still available to buy assets. Default implementation is just the
     * amount of available cash. But with margin accounts there is typically more money available to
     * trade than just the total remaining cash balance.
     *
     * @return the available buying power
     */
    var buyingPower: Double = Double.NaN
        get() = if (field.isNaN()) getTotalCash() else field

    /**
     * Reset the account to its initial state.
     */
    fun reset() {
        time = Instant.MIN
        trades.clear()
        orders.clear()
        portfolio.clear()
        cash.clear()
    }


    /**
     * Get the total cash balance hold in this account, converted to a single currency amount.
     *
     * @param currency The currency to convert to, default is the account base currency
     * @param now The time to use for the conversion, default is the account last update time
     * @return The total amount
     */
    fun getTotalCash(currency: Currency = baseCurrency, now: Instant = time): Double {
        return convertToCurrency(cash, currency, now)
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
        s.add("buying power", baseCurrency.format(buyingPower))
        val value = convertToCurrency(getValue())
        s.add("total value", baseCurrency.format(value))

        val tradesSummary = Summary("Trades")
        tradesSummary.add("total", trades.size)
        val realizedPNL = convertToCurrency(trades.realizedPnL())
        tradesSummary.add("realized p&l", baseCurrency.format(realizedPNL))
        tradesSummary.add("assets", trades.assets.size)
        val fee = convertToCurrency(trades.totalFee())
        tradesSummary.add("fee", baseCurrency.format(fee))
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
        val unrealizedValue = convertToCurrency(portfolio.getValue())
        portfolioSummary.add("estimated value", baseCurrency.format(unrealizedValue))
        val unrealizedPNL = convertToCurrency(portfolio.unrealizedPNL())
        portfolioSummary.add("unrealized p&l", baseCurrency.format(unrealizedPNL))
        s.add(portfolioSummary)

        s.add(cash.summary())
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
        s.add("buying power", baseCurrency.format(buyingPower))
        val value = convertToCurrency(getValue())
        s.add("total value", baseCurrency.format(value))

        s.add(cash.summary())
        s.add(portfolio.summary())
        s.add(orders.summary())
        s.add(trades.summary())
        return s
    }

    /**
     * Convert a [Cash] value into a single currency amount. If no currencyConverter has been configured and this method is
     * called and a conversion is required, it will throw a [ConfigurationException].
     *
     * @param cash The cash values to convert from
     * @param toCurrency The currency to convert the cash to, default is the baseCurrency of the account
     * @param now The time to use for the exchange rate, default is the last update time of the account
     * @return The converted amount as a Double
     */
    fun convertToCurrency(cash: Cash, toCurrency: Currency = baseCurrency, now: Instant = time): Double {
        var sum = 0.0
        cash.toMap().forEach { (fromCurrency, amount) ->
            sum += if (fromCurrency === toCurrency) {
                amount
            } else {
                currencyConverter?.convert(fromCurrency, toCurrency, amount, now)
                    ?: throw ConfigurationException("No currency converter defined to convert from $fromCurrency to $toCurrency")
            }
        }
        return sum
    }

    fun convertToCurrency(
        fromCurrency: Currency,
        amount: Double,
        toCurrency: Currency = baseCurrency,
        now: Instant = time
    ): Double {
        return if (fromCurrency === toCurrency || amount == 0.0) {
            amount
        } else {
            currencyConverter?.convert(fromCurrency, toCurrency, amount, now)
                ?: throw ConfigurationException("No currency converter defined to convert from $fromCurrency to $toCurrency")
        }
    }

    /**
     * Get the total value of this account, where the total account value is defined as the sum of all
     * the portfolio positions and the total of the cash balances.
     *
     *      Account Value = Portfolio Value + Cash
     *
     * The portfolio positions are evaluated against the last known price for that position and thus reflect the
     * unrealized P&L.
     *
     * @return the total value of the account as [Cash]
     */
    fun getValue(): Cash {
        val result = portfolio.getValue()
        result.deposit(cash)
        return result
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
        account.trades.addAll(trades)
        account.orders.put(orders)
        account.cash.clear()
        account.cash.deposit(cash)
        account.portfolio.put(portfolio)
        account.buyingPower = buyingPower
        return account
    }


}

