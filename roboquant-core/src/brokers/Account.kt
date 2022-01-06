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
    private val usageCalculator: UsageCalculator = BasicUsageCalculator()
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
    val cash : Cash = Cash()


    val cashAmount : Double
        get() = convertToCurrency(cash)



    var buyingPower : Double = Double.NaN
        get() = if (field.isNaN()) cashAmount else field


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
        get() = convertToCurrency(equity)


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
     * Get the margin requirements for provided portfolio
     */
    fun getMarginRequirements(portfolio: Portfolio = this.portfolio) : Cash {
        return usageCalculator.calculate(portfolio.positions)
    }

    /**
     * Get the total cash balance hold in this account converted to a single currency amount.
     *
     * @param currency The currency to convert to, default is the account base currency
     * @param now The time to use for the conversion, default is the account last update time
     * @return The total amount
     */
    fun getTotalCash(currency: Currency = baseCurrency, now: Instant = time): Double {
        return convertToCurrency(cash, currency, now)
    }

    /**
     * Summary overview of the cash positions
     */
    fun cashSummary(): Summary {
        val result = Summary("Cash")
        val fmt = "│%10s│%14s│"
        val header = String.format(fmt, "currency", "amount")
        result.add(header)
        val currencies = cash.currencies
        for (currency in currencies.distinct().sortedBy { it.displayName }) {
            val t =  currency.format(cash.getAmount(currency))
            val line = String.format(fmt,  currency.currencyCode, t)
            result.add(line)
        }
        return result
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
        s.add(cashSummary())

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
        val unrealizedValue = convertToCurrency(portfolio.value)
        portfolioSummary.add("estimated value", baseCurrency.format(unrealizedValue))
        val unrealizedPNL = convertToCurrency(portfolio.unrealizedPNL())
        portfolioSummary.add("unrealized p&l", baseCurrency.format(unrealizedPNL))
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
        s.add("buying power", baseCurrency.format(buyingPower))
        s.add(cashSummary())
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
        return account
    }


}

